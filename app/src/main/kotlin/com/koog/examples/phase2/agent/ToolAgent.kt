package com.koog.examples.phase2.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.koog.examples.phase2.config.Phase2Config
import com.koog.examples.phase2.tools.NewsTools
import com.koog.examples.phase2.tools.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.jvm.java

@Component
class ToolAgent(
    private val config: Phase2Config,
    private val weatherTools: WeatherTools,
    private val newsTools: NewsTools,
    @param:Value("\${api.google-api-key}")
    private val googleApiKey: String,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // ToolRegistryを作成してツールを登録
    private val toolRegistry = ToolRegistry {
        // ビルトインツール
        tool(AskUser)
        tool(SayToUser)

        // カスタムツール（反射ベースのツールセット）
        tools(weatherTools.asTools())
        tools(newsTools.asTools())
        
        // SimpleToolベースのツール
        tool(UUIDGeneratorTool)
        
        // Toolクラスベースのツール
        tool(Base64EncoderTool)
    }

    // ToolRegistryを使用してAIAgentを作成
    private fun createAgent() = AIAgent(
        llmModel = config.llmModel,
        executor = simpleGoogleAIExecutor(googleApiKey),
        systemPrompt = config.systemPrompt,
        temperature = config.temperature,
        toolRegistry = toolRegistry,
        maxIterations = 10
    )

    suspend fun process(userMessage: String): String {
        logger.info("Processing user message: $userMessage")

        return try {
            val agent = createAgent()
            val response = agent.run(userMessage)
            logger.info("Agent response generated successfully")
            response
        } catch (e: Exception) {
            logger.error("Error processing message", e)
            "エラーが発生しました: ${e.message}"
        }
    }

    fun getAvailableTools(): List<ToolInfo> {
        // ToolRegistryからツール情報を動的に取得
        return toolRegistry.tools.map { tool ->
            // descriptorから包括的なメタデータを取得
            val descriptor = tool.descriptor
            val toolName = descriptor.name
            val toolDescription = descriptor.description

            val parameters = when (toolName) {
                "askUser" -> listOf(
                    ParameterInfo("question", "string", "ユーザーへの質問", true)
                )
                "sayToUser" -> listOf(
                    ParameterInfo("message", "string", "ユーザーへのメッセージ", true)
                )
                "getWeather" -> listOf(
                    ParameterInfo("city", "string", "天気を取得したい都市名（日本語または英語）", true)
                )
                "getWeatherWithAdvice" -> listOf(
                    ParameterInfo("city", "string", "天気を取得したい都市名", true)
                )
                "searchNews" -> listOf(
                    ParameterInfo("query", "string", "検索キーワード", true),
                    ParameterInfo("limit", "integer", "取得する記事数（1-20、デフォルト: 5）", false)
                )
                "getTopHeadlines" -> listOf(
                    ParameterInfo("country", "string", "国コード（jp=日本、us=アメリカなど）", false),
                    ParameterInfo(
                        "category",
                        "string",
                        "カテゴリー（business, entertainment, general, health, science, sports, technology）",
                        false
                    ),
                    ParameterInfo("limit", "integer", "取得する記事数（1-20）", false)
                )
                "uuid_generator" -> listOf(
                    ParameterInfo("count", "integer", "生成するUUIDの個数（1-10、デフォルト: 1）", false),
                    ParameterInfo("format", "string", "UUIDのフォーマット（standard, compact, uppercase）", false)
                )
                "base64_encoder" -> listOf(
                    ParameterInfo("text", "string", "エンコード/デコードするテキスト", true),
                    ParameterInfo("operation", "string", "実行する操作（encode または decode）", false),
                    ParameterInfo("urlSafe", "boolean", "URLセーフなBase64を使用するか", false)
                )
                else -> emptyList()
            }

            ToolInfo(
                name = toolName,
                description = toolDescription,
                parameters = parameters
            )
        }.also { tools ->
            logger.debug("Available tools: {}", tools.map { it.name })
        }
    }

    data class ToolInfo(
        val name: String,
        val description: String,
        val parameters: List<ParameterInfo>
    )

    data class ParameterInfo(
        val name: String,
        val type: String,
        val description: String,
        val required: Boolean
    )
}
