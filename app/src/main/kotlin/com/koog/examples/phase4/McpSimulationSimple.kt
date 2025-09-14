package com.koog.examples.phase4

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.clients.google.GoogleModels
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

private val logger = KotlinLogging.logger {}


/**
 * Phase 4: MCP (Model Context Protocol) シミュレーション - シンプル版
 *
 * 注意: Koog 0.4.1では正式なMCPサポートがまだリリースされていません。
 * 将来のバージョンでは ai.koog.agents.mcp パッケージが利用可能になる予定です。
 *
 * 現在は SimpleTool と ToolRegistry を使用してMCPのような機能を実装しています。
 */
@Component
class McpSimulationSimple {

    /**
     * MCPシミュレーションの実行
     */
    suspend fun runMcpSimulation() {
        logger.info { "Starting MCP simulation..." }

        try {
            val apiKey = System.getenv("GOOGLE_API_KEY")
                ?: throw IllegalStateException("GOOGLE_API_KEY not set")

            // ツールレジストリの作成
            val toolRegistry = createMcpSimulatedRegistry()
            logger.info { "Tool registry created with ${toolRegistry.tools.size} tools" }

            // エージェントの作成
            val agent = AIAgent(
                llmModel = GoogleModels.Gemini2_0Flash001,
                executor = simpleGoogleAIExecutor(apiKey),
                systemPrompt = """
                    You are an AI assistant with access to tools.
                    Available tools:
                    - echo: Echo messages back
                    - time: Get current time in various formats
                    - calculator: Perform basic calculations

                    Use these tools to help with user tasks.
                """.trimIndent(),
                temperature = 0.7,
                toolRegistry = toolRegistry,
                maxIterations = 5
            )

            // テストタスクの実行
            val tasks = listOf(
                "Echo the message 'Hello MCP Simulation!'",
                "What time is it now in ISO format?",
                "Calculate 42 + 58"
            )

            for (task in tasks) {
                logger.info { "Executing task: $task" }
                val result = agent.run(task)
                logger.info { "Result: $result" }
            }

        } catch (e: Exception) {
            logger.error(e) { "Failed to run MCP simulation" }
        }
    }

    /**
     * MCPツールレジストリの作成
     */
    private fun createMcpSimulatedRegistry(): ToolRegistry {
        return ToolRegistry {
            // エコーツール
            tool(EchoTool)
            // 時刻取得ツール
            tool(TimeTool)
            // 計算ツール
            tool(CalculatorTool)
        }
    }

    /**
     * ツール情報の表示
     */
    fun displayToolInfo() {
        logger.info { "MCP Simulated Tools:" }
        val registry = createMcpSimulatedRegistry()

        registry.tools.forEach { tool ->
            logger.info {
                """
                ═══════════════════════════════════════
                Tool: ${tool.descriptor.name}
                Description: ${tool.descriptor.description}
                Required params: ${tool.descriptor.requiredParameters.size}
                Optional params: ${tool.descriptor.optionalParameters.size}
                ═══════════════════════════════════════
                """.trimIndent()
            }
        }
    }
}

// ========== MCP シミュレートツール ==========

/**
 * エコーツール
 */
object EchoTool : SimpleTool<EchoTool.Args>() {
    @Serializable
    data class Args(
        val message: String
    ) : ToolArgs

    override val argsSerializer = kotlinx.serialization.serializer<Args>()

    override val descriptor = ToolDescriptor(
        name = "echo",
        description = "Echo a message back to the user",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "message",
                description = "The message to echo",
                type = ToolParameterType.String
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        logger.info { "Echo tool called with: ${args.message}" }
        return "Echo: ${args.message}"
    }
}

/**
 * 時刻取得ツール
 */
object TimeTool : SimpleTool<TimeTool.Args>() {
    @Serializable
    data class Args(
        val format: String = "iso"
    ) : ToolArgs

    override val argsSerializer = kotlinx.serialization.serializer<Args>()

    override val descriptor = ToolDescriptor(
        name = "time",
        description = "Get the current time in various formats",
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "format",
                description = "Time format: iso, unix, or millis (default: iso)",
                type = ToolParameterType.String
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        val now = Instant.now()
        val result = when (args.format.lowercase()) {
            "unix" -> "Unix timestamp: ${now.epochSecond}"
            "millis" -> "Milliseconds: ${now.toEpochMilli()}"
            else -> "ISO format: $now"
        }
        logger.info { "Time tool result: $result" }
        return result
    }
}

/**
 * 計算ツール
 */
object CalculatorTool : SimpleTool<CalculatorTool.Args>() {
    @Serializable
    data class Args(
        val a: Double,
        val b: Double,
        val operation: String = "add"
    ) : ToolArgs

    override val argsSerializer = kotlinx.serialization.serializer<Args>()

    override val descriptor = ToolDescriptor(
        name = "calculator",
        description = "Perform basic mathematical calculations",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "a",
                description = "First number",
                type = ToolParameterType.Integer
            ),
            ToolParameterDescriptor(
                name = "b",
                description = "Second number",
                type = ToolParameterType.Integer
            )
        ),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "operation",
                description = "Operation: add, subtract, multiply, divide (default: add)",
                type = ToolParameterType.String
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        val result = when (args.operation.lowercase()) {
            "subtract" -> args.a - args.b
            "multiply" -> args.a * args.b
            "divide" -> if (args.b != 0.0) args.a / args.b else Double.NaN
            else -> args.a + args.b
        }

        val expression = "${args.a} ${args.operation} ${args.b} = $result"
        logger.info { "Calculator tool: $expression" }
        return expression
    }
}

/**
 * メイン実行関数
 */
fun main() = runBlocking {
    val simulation = McpSimulationSimple()

    // ツール情報の表示
    simulation.displayToolInfo()

    // MCPシミュレーションの実行
    simulation.runMcpSimulation()
}