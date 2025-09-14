package com.koog.examples.phase4

import ai.koog.agents.agent.AIAgent
import ai.koog.agents.llm.openai.OpenAIModels
import ai.koog.agents.mcp.McpTool
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.prompts.Prompt
import ai.koog.agents.prompts.SimplePromptExecutor
import ai.koog.agents.strategy.agent.graph.StringSubgraphResult
import ai.koog.agents.strategy.agent.graph.agentStrategies
import ai.koog.agents.tools.Tool
import ai.koog.agents.tools.ToolRegistry
import ai.koog.agents.tools.tool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Phase 4: MCPツール統合の実装
 *
 * MCPツールとカスタムツールを組み合わせた高度な統合例
 */
@Component
class McpToolIntegration {

    private val json = Json { prettyPrint = true }

    /**
     * MCPツールとカスタムツールを組み合わせたツールレジストリの作成
     */
    suspend fun createHybridToolRegistry(): ToolRegistry {
        logger.info { "Creating hybrid tool registry with MCP and custom tools..." }

        // MCPツールレジストリの取得
        val mcpTransport = McpToolRegistryProvider.defaultSseTransport(
            url = "http://localhost:8931/sse"
        )

        val mcpRegistry = McpToolRegistryProvider.fromTransport(
            transport = mcpTransport,
            name = "hybrid-client",
            version = "1.0.0"
        )

        // カスタムツールの定義
        val customTools = listOf(
            createWeatherTool(),
            createCalculatorTool(),
            createDataAnalysisTool()
        )

        // ハイブリッドツールレジストリの作成
        val hybridRegistry = ToolRegistry(
            tools = mcpRegistry.tools + customTools.associateBy { it.name }
        )

        logger.info {
            """
            Hybrid Tool Registry created:
            - MCP Tools: ${mcpRegistry.tools.keys}
            - Custom Tools: ${customTools.map { it.name }}
            - Total Tools: ${hybridRegistry.tools.size}
            """.trimIndent()
        }

        return hybridRegistry
    }

    /**
     * 天気情報取得ツール
     */
    private fun createWeatherTool(): Tool<WeatherInput, WeatherOutput> {
        return tool(
            name = "get_weather",
            description = "Get current weather information for a city",
            inputType = WeatherInput::class,
            outputType = WeatherOutput::class
        ) { input ->
            logger.info { "Getting weather for ${input.city}" }
            // 実際のAPIコールの代わりにモックデータを返す
            WeatherOutput(
                city = input.city,
                temperature = 22.5,
                condition = "Partly Cloudy",
                humidity = 65
            )
        }
    }

    /**
     * 計算ツール
     */
    private fun createCalculatorTool(): Tool<CalculatorInput, CalculatorOutput> {
        return tool(
            name = "calculator",
            description = "Perform mathematical calculations",
            inputType = CalculatorInput::class,
            outputType = CalculatorOutput::class
        ) { input ->
            logger.info { "Calculating: ${input.expression}" }
            val result = when (input.operation) {
                "add" -> input.a + input.b
                "subtract" -> input.a - input.b
                "multiply" -> input.a * input.b
                "divide" -> if (input.b != 0.0) input.a / input.b else Double.NaN
                else -> Double.NaN
            }
            CalculatorOutput(
                expression = input.expression,
                result = result
            )
        }
    }

    /**
     * データ分析ツール
     */
    private fun createDataAnalysisTool(): Tool<DataAnalysisInput, DataAnalysisOutput> {
        return tool(
            name = "analyze_data",
            description = "Analyze data and provide insights",
            inputType = DataAnalysisInput::class,
            outputType = DataAnalysisOutput::class
        ) { input ->
            logger.info { "Analyzing data: ${input.dataPoints.size} points" }

            val mean = input.dataPoints.average()
            val max = input.dataPoints.maxOrNull() ?: 0.0
            val min = input.dataPoints.minOrNull() ?: 0.0
            val stdDev = calculateStandardDeviation(input.dataPoints, mean)

            DataAnalysisOutput(
                mean = mean,
                max = max,
                min = min,
                standardDeviation = stdDev,
                dataPointCount = input.dataPoints.size
            )
        }
    }

    private fun calculateStandardDeviation(data: List<Double>, mean: Double): Double {
        if (data.isEmpty()) return 0.0
        val variance = data.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }

    /**
     * MCPツールの実行例
     */
    suspend fun executeMcpTool() {
        logger.info { "Executing MCP tool..." }

        val transport = McpToolRegistryProvider.defaultSseTransport(
            url = "http://localhost:8931/sse"
        )

        val toolRegistry = McpToolRegistryProvider.fromTransport(
            transport = transport,
            name = "tool-executor",
            version = "1.0.0"
        )

        // 最初のMCPツールを取得して実行
        val mcpTool = toolRegistry.tools.values.firstOrNull() as? McpTool
        if (mcpTool != null) {
            logger.info { "Executing tool: ${mcpTool.name}" }

            // ツールの実行（入力パラメータは実際のツールに応じて調整）
            val input = mapOf("query" to "test")
            val result = mcpTool.execute(json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(),
                kotlinx.serialization.json.buildJsonObject {
                    input.forEach { (k, v) -> put(k, kotlinx.serialization.json.JsonPrimitive(v)) }
                }
            ))

            logger.info { "Tool execution result: $result" }
        } else {
            logger.warn { "No MCP tools available" }
        }
    }

    /**
     * ハイブリッドエージェントの作成と実行
     */
    suspend fun runHybridAgent() {
        logger.info { "Running hybrid agent with MCP and custom tools..." }

        val toolRegistry = createHybridToolRegistry()

        val executor = SimplePromptExecutor(
            apiKey = System.getenv("OPENAI_API_KEY") ?: throw IllegalStateException("OPENAI_API_KEY not set")
        )

        val strategy = agentStrategies {
            graph<StringSubgraphResult> {
                val result = agent {
                    name = "hybrid-agent"
                    description = "Agent with both MCP and custom tools"
                    prompt = """
                        You are an AI assistant with access to various tools.
                        You can:
                        1. Get weather information
                        2. Perform calculations
                        3. Analyze data
                        4. Use MCP tools for additional functionality

                        Help the user with their tasks using the most appropriate tools.
                    """.trimIndent()
                    outputSchema = StringSubgraphResult::class
                }
                result
            }
        }

        val agent = AIAgent(
            executor = executor,
            strategy = strategy,
            llmModel = OpenAIModels.Chat.GPT4o,
            toolRegistry = toolRegistry
        )

        // 複数のタスクを実行
        val tasks = listOf(
            "What's the weather in Tokyo?",
            "Calculate 15% tip on a $85 restaurant bill",
            "Analyze the trend in these numbers: 10, 15, 13, 18, 22, 19, 25"
        )

        for (task in tasks) {
            logger.info { "Task: $task" }
            val result = agent.execute(Prompt(task))
            logger.info { "Result: $result" }
        }
    }
}

// データクラス定義
@Serializable
data class WeatherInput(val city: String)

@Serializable
data class WeatherOutput(
    val city: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int
)

@Serializable
data class CalculatorInput(
    val expression: String,
    val operation: String,
    val a: Double,
    val b: Double
)

@Serializable
data class CalculatorOutput(
    val expression: String,
    val result: Double
)

@Serializable
data class DataAnalysisInput(
    val dataPoints: List<Double>
)

@Serializable
data class DataAnalysisOutput(
    val mean: Double,
    val max: Double,
    val min: Double,
    val standardDeviation: Double,
    val dataPointCount: Int
)

/**
 * メイン実行関数
 */
fun main() = runBlocking {
    val integration = McpToolIntegration()

    // ハイブリッドエージェントの実行
    integration.runHybridAgent()

    // MCPツールの直接実行
    integration.executeMcpTool()
}