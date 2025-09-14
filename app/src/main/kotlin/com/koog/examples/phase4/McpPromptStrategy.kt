package com.koog.examples.phase4

import ai.koog.agents.agent.AIAgent
import ai.koog.agents.llm.openai.OpenAIModels
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.prompts.Prompt
import ai.koog.agents.prompts.SimplePromptExecutor
import ai.koog.agents.strategy.agent.graph.StringSubgraphResult
import ai.koog.agents.strategy.agent.graph.agentStrategies
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Phase 4: MCPプロンプト戦略の実装
 *
 * MCPプロンプトテンプレートを活用した動的プロンプト生成とコンテキスト管理
 */
@Component
class McpPromptStrategy {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // プロンプトテンプレートのリポジトリ
    private val promptTemplates = mutableMapOf<String, PromptTemplate>()

    // コンテキストストア
    private val contextStore = mutableMapOf<String, Any>()

    init {
        // 初期プロンプトテンプレートの登録
        registerDefaultTemplates()
    }

    /**
     * デフォルトプロンプトテンプレートの登録
     */
    private fun registerDefaultTemplates() {
        // 分析用テンプレート
        promptTemplates["analysis"] = PromptTemplate(
            id = "analysis",
            name = "Data Analysis Template",
            template = """
                You are a data analyst. Analyze the following data:
                {{data}}

                Consider these aspects:
                {{#aspects}}
                - {{.}}
                {{/aspects}}

                Provide insights in the following format:
                1. Summary
                2. Key Findings
                3. Recommendations

                {{#context}}
                Additional Context: {{context}}
                {{/context}}
            """.trimIndent(),
            variables = listOf("data", "aspects", "context"),
            tags = listOf("analysis", "data", "insights")
        )

        // 質問応答用テンプレート
        promptTemplates["qa"] = PromptTemplate(
            id = "qa",
            name = "Question Answering Template",
            template = """
                Answer the following question based on the provided context:

                Question: {{question}}

                {{#documents}}
                Context Documents:
                {{documents}}
                {{/documents}}

                {{#history}}
                Previous Conversation:
                {{history}}
                {{/history}}

                Provide a clear, concise, and accurate answer.
                If the information is not available in the context, say so.
            """.trimIndent(),
            variables = listOf("question", "documents", "history"),
            tags = listOf("qa", "rag", "context")
        )

        // タスク実行用テンプレート
        promptTemplates["task"] = PromptTemplate(
            id = "task",
            name = "Task Execution Template",
            template = """
                Execute the following task:
                {{task_description}}

                {{#requirements}}
                Requirements:
                {{#requirements_list}}
                - {{.}}
                {{/requirements_list}}
                {{/requirements}}

                {{#tools}}
                Available Tools:
                {{#tool_list}}
                - {{name}}: {{description}}
                {{/tool_list}}
                {{/tools}}

                {{#constraints}}
                Constraints:
                {{constraints}}
                {{/constraints}}

                Execute step by step and provide clear results.
            """.trimIndent(),
            variables = listOf("task_description", "requirements", "tools", "constraints"),
            tags = listOf("task", "execution", "tools")
        )

        // コード生成用テンプレート
        promptTemplates["code_generation"] = PromptTemplate(
            id = "code_generation",
            name = "Code Generation Template",
            template = """
                Generate {{language}} code for the following requirement:
                {{requirement}}

                {{#specifications}}
                Technical Specifications:
                - Language: {{language}}
                - Framework: {{framework}}
                - Style Guide: {{style}}
                {{/specifications}}

                {{#examples}}
                Reference Examples:
                ```{{language}}
                {{example_code}}
                ```
                {{/examples}}

                Generate clean, efficient, and well-documented code.
                Include error handling and follow best practices.
            """.trimIndent(),
            variables = listOf("requirement", "language", "framework", "style", "example_code"),
            tags = listOf("code", "generation", "programming")
        )

        logger.info { "Registered ${promptTemplates.size} default prompt templates" }
    }

    /**
     * プロンプトの生成
     */
    fun generatePrompt(
        templateId: String,
        variables: Map<String, Any>
    ): String {
        val template = promptTemplates[templateId]
            ?: throw IllegalArgumentException("Template not found: $templateId")

        var prompt = template.template

        // 変数の置換
        variables.forEach { (key, value) ->
            when (value) {
                is List<*> -> {
                    // リスト変数の処理
                    val listContent = value.joinToString("\n") { "- $it" }
                    prompt = prompt.replace("{{#$key}}{{#${key}_list}}- {{.}}{{/${key}_list}}{{/$key}}", listContent)
                }
                is Map<*, *> -> {
                    // マップ変数の処理
                    value.forEach { (k, v) ->
                        prompt = prompt.replace("{{$k}}", v.toString())
                    }
                }
                else -> {
                    // 単純な変数の置換
                    prompt = prompt.replace("{{$key}}", value.toString())
                }
            }
        }

        // 未使用の条件ブロックを削除
        prompt = prompt.replace(Regex("\\{\\{#\\w+\\}\\}[^{]*\\{\\{/\\w+\\}\\}"), "")

        logger.debug { "Generated prompt from template '$templateId'" }
        return prompt.trim()
    }

    /**
     * コンテキスト対応プロンプトの生成
     */
    fun generateContextAwarePrompt(
        templateId: String,
        variables: Map<String, Any>,
        contextKeys: List<String> = emptyList()
    ): String {
        // コンテキストから追加変数を取得
        val contextVariables = contextKeys.associateWith { key ->
            contextStore[key] ?: ""
        }

        // 変数をマージ
        val allVariables = variables + contextVariables

        return generatePrompt(templateId, allVariables)
    }

    /**
     * コンテキストの更新
     */
    fun updateContext(key: String, value: Any) {
        contextStore[key] = value
        logger.debug { "Updated context: $key" }
    }

    /**
     * プロンプトチェーンの実行
     */
    suspend fun executePromptChain(
        chain: List<PromptChainStep>
    ): List<String> {
        logger.info { "Executing prompt chain with ${chain.size} steps" }

        val results = mutableListOf<String>()
        val chainContext = mutableMapOf<String, Any>()

        for ((index, step) in chain.withIndex()) {
            logger.info { "Executing step ${index + 1}: ${step.name}" }

            // 前のステップの結果をコンテキストに追加
            if (index > 0 && step.usePreviousResult) {
                chainContext["previous_result"] = results.last()
            }

            // プロンプトの生成
            val prompt = generatePrompt(
                templateId = step.templateId,
                variables = step.variables + chainContext
            )

            // エージェントの実行（簡略化）
            val result = executeWithAgent(prompt)
            results.add(result)

            // 結果をチェーンコンテキストに保存
            step.outputKey?.let {
                chainContext[it] = result
            }
        }

        return results
    }

    /**
     * エージェントでプロンプトを実行（簡略化版）
     */
    private suspend fun executeWithAgent(prompt: String): String {
        val executor = SimplePromptExecutor(
            apiKey = System.getenv("OPENAI_API_KEY") ?: throw IllegalStateException("OPENAI_API_KEY not set")
        )

        val strategy = agentStrategies {
            graph<StringSubgraphResult> {
                val result = agent {
                    name = "prompt-executor"
                    description = "Execute generated prompt"
                    this.prompt = prompt
                    outputSchema = StringSubgraphResult::class
                }
                result
            }
        }

        val agent = AIAgent(
            executor = executor,
            strategy = strategy,
            llmModel = OpenAIModels.Chat.GPT4o,
            toolRegistry = null
        )

        val result = agent.execute(Prompt(""))
        return result.toString()
    }

    /**
     * 動的プロンプト戦略の実行例
     */
    suspend fun runDynamicPromptStrategy() {
        logger.info { "Running dynamic prompt strategy..." }

        // 1. 分析タスクの実行
        val analysisPrompt = generatePrompt(
            templateId = "analysis",
            variables = mapOf(
                "data" to "Sales: Q1=$1.2M, Q2=$1.5M, Q3=$1.3M, Q4=$1.8M",
                "aspects" to listOf("Trends", "Seasonality", "Growth Rate"),
                "context" to "Previous year total: $4.5M"
            )
        )
        logger.info { "Analysis Prompt:\n$analysisPrompt" }

        // 2. プロンプトチェーンの実行
        val chain = listOf(
            PromptChainStep(
                name = "Data Collection",
                templateId = "task",
                variables = mapOf(
                    "task_description" to "Collect quarterly performance metrics",
                    "requirements_list" to listOf("Accuracy", "Completeness", "Timeliness")
                ),
                outputKey = "metrics_data"
            ),
            PromptChainStep(
                name = "Data Analysis",
                templateId = "analysis",
                variables = mapOf(
                    "data" to "{{metrics_data}}",
                    "aspects" to listOf("Performance", "Efficiency", "ROI")
                ),
                usePreviousResult = true,
                outputKey = "analysis_result"
            ),
            PromptChainStep(
                name = "Report Generation",
                templateId = "task",
                variables = mapOf(
                    "task_description" to "Generate executive summary based on analysis",
                    "requirements_list" to listOf("Concise", "Actionable", "Visual")
                ),
                usePreviousResult = true
            )
        )

        val chainResults = executePromptChain(chain)
        chainResults.forEachIndexed { index, result ->
            logger.info { "Chain Step ${index + 1} Result: ${result.take(100)}..." }
        }

        // 3. コンテキスト対応プロンプトの生成
        updateContext("user_preference", "detailed analysis with visualizations")
        updateContext("domain", "financial services")

        val contextAwarePrompt = generateContextAwarePrompt(
            templateId = "qa",
            variables = mapOf(
                "question" to "What are the key performance indicators?",
                "documents" to "Quarterly reports and financial statements"
            ),
            contextKeys = listOf("user_preference", "domain")
        )
        logger.info { "Context-Aware Prompt:\n$contextAwarePrompt" }
    }

    /**
     * プロンプトテンプレートの管理機能
     */
    fun manageTemplates() {
        // テンプレートの一覧表示
        logger.info { "Available templates:" }
        promptTemplates.values.forEach { template ->
            logger.info {
                """
                - ${template.name} (${template.id})
                  Variables: ${template.variables.joinToString(", ")}
                  Tags: ${template.tags.joinToString(", ")}
                """.trimIndent()
            }
        }

        // テンプレートの検索
        val analysisTemplates = findTemplatesByTag("analysis")
        logger.info { "Templates with 'analysis' tag: ${analysisTemplates.map { it.id }}" }
    }

    /**
     * タグによるテンプレート検索
     */
    private fun findTemplatesByTag(tag: String): List<PromptTemplate> {
        return promptTemplates.values.filter { it.tags.contains(tag) }
    }
}

/**
 * プロンプトテンプレート
 */
@Serializable
data class PromptTemplate(
    val id: String,
    val name: String,
    val template: String,
    val variables: List<String>,
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * プロンプトチェーンステップ
 */
data class PromptChainStep(
    val name: String,
    val templateId: String,
    val variables: Map<String, Any>,
    val usePreviousResult: Boolean = false,
    val outputKey: String? = null
)

/**
 * メイン実行関数
 */
fun main() = runBlocking {
    val strategy = McpPromptStrategy()

    // プロンプト戦略の実行
    strategy.runDynamicPromptStrategy()

    // テンプレート管理
    strategy.manageTemplates()
}