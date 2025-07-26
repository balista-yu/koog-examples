package com.koog.examples.phase2.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import com.koog.examples.phase2.config.Phase2Config
import com.koog.examples.phase2.tools.NewsTools
import com.koog.examples.phase2.tools.TextAnalysisTools
import com.koog.examples.phase2.tools.WeatherTools
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ToolAgent(
    private val config: Phase2Config,
    private val googleExecutor: SingleLLMPromptExecutor,
    private val weatherTools: WeatherTools,
    private val newsTools: NewsTools,
    private val textAnalysisTools: TextAnalysisTools
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
        tools(textAnalysisTools.asTools())
    }

    // ToolRegistryを使用してAIAgentを作成
    private fun createAgent() = AIAgent(
        llmModel = config.llmModel,
        executor = googleExecutor,
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
        // 現在はToolRegistryから動的に取得するのが難しいので、
        // 手動でツール情報を定義する
        val toolsList = mutableListOf<ToolInfo>()

        // ビルトインツール
        toolsList.add(ToolInfo(
            name = "askUser",
            description = "ユーザーに質問をする",
            parameters = listOf(
                ParameterInfo("question", "string", "ユーザーへの質問", true)
            )
        ))
        toolsList.add(ToolInfo(
            name = "sayToUser",
            description = "ユーザーに情報を伝える",
            parameters = listOf(
                ParameterInfo("message", "string", "ユーザーへのメッセージ", true)
            )
        ))

        // Weather Tools
        toolsList.add(ToolInfo(
            name = "getWeather",
            description = "指定された都市の現在の天気情報を取得します",
            parameters = listOf(
                ParameterInfo("city", "string", "天気を取得したい都市名（日本語または英語）", true)
            )
        ))
        toolsList.add(ToolInfo(
            name = "getWeatherWithAdvice",
            description = "指定された都市の天気情報を取得し、アドバイスも提供します",
            parameters = listOf(
                ParameterInfo("city", "string", "天気を取得したい都市名", true)
            )
        ))

        // News Tools
        toolsList.add(ToolInfo(
            name = "searchNews",
            description = "キーワードに基づいてニュースを検索します",
            parameters = listOf(
                ParameterInfo("query", "string", "検索キーワード", true),
                ParameterInfo("limit", "integer", "取得する記事数（1-20、デフォルト: 5）", false)
            )
        ))
        toolsList.add(ToolInfo(
            name = "getTopHeadlines",
            description = "最新のヘッドラインニュースを取得します",
            parameters = listOf(
                ParameterInfo("country", "string", "国コード（jp=日本、us=アメリカなど）", false),
                ParameterInfo(
                    "category",
                    "string",
                    "カテゴリー（business, entertainment, general, health, science, sports, technology）",
                    false
                ),
                ParameterInfo("limit", "integer", "取得する記事数（1-20）", false)
            )
        ))

        // Text Analysis Tools
        toolsList.add(ToolInfo(
            name = "analyzeText",
            description = "テキストの基本的な統計情報を分析します",
            parameters = listOf(
                ParameterInfo("text", "string", "分析するテキスト", true)
            )
        ))
        toolsList.add(ToolInfo(
            name = "extractPatterns",
            description = "テキスト内の URL、メールアドレス、ハッシュタグなどを抽出します",
            parameters = listOf(
                ParameterInfo("text", "string", "パターンを抽出するテキスト", true)
            )
        ))
        toolsList.add(ToolInfo(
            name = "analyzeCharacterTypes",
            description = "テキストの文字種別（ひらがな、カタカナ、漢字など）を分析します",
            parameters = listOf(
                ParameterInfo("text", "string", "文字種別を分析するテキスト", true)
            )
        ))

        return toolsList
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
