package com.koog.examples.phase4

import ai.koog.agents.agent.AIAgent
import ai.koog.agents.llm.openai.OpenAIModels
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.prompts.Prompt
import ai.koog.agents.prompts.SimplePromptExecutor
import ai.koog.agents.strategy.agent.graph.StringSubgraphResult
import ai.koog.agents.strategy.agent.graph.agentStrategies
import ai.koog.agents.tools.ToolRegistry
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Phase 4: 基本的なMCP統合の例
 *
 * MCPサーバーに接続し、ツールを取得してKoogエージェントで使用する基本的な実装例です。
 */
@Component
class McpBasicExample {

    /**
     * SSEトランスポートを使用したMCP接続の例
     */
    suspend fun runSseExample() {
        logger.info { "Starting MCP SSE Example..." }

        try {
            // SSEトランスポートの作成
            val transport = McpToolRegistryProvider.defaultSseTransport(
                url = "http://localhost:8931/sse"
            )

            // MCPツールレジストリの作成
            val toolRegistry = McpToolRegistryProvider.fromTransport(
                transport = transport,
                name = "koog-mcp-client",
                version = "1.0.0"
            )

            logger.info { "Connected to MCP server. Available tools: ${toolRegistry.tools.keys}" }

            // エージェントの作成
            val agent = createAgentWithMcpTools(toolRegistry)

            // MCPツールを使用したタスクの実行
            val result = agent.execute(
                Prompt("List all available MCP tools and describe their functionality")
            )

            logger.info { "Agent response: $result" }

        } catch (e: Exception) {
            logger.error(e) { "Failed to connect to MCP server" }
        }
    }

    /**
     * プロセス（stdio）トランスポートを使用したMCP接続の例
     */
    suspend fun runProcessExample() {
        logger.info { "Starting MCP Process Example..." }

        try {
            // MCPサーバープロセスの起動
            val process = ProcessBuilder(
                "node",
                "/path/to/mcp-server.js"
            ).start()

            // プロセストランスポートを使用したツールレジストリの作成
            val transport = McpToolRegistryProvider.defaultProcessTransport(process)

            val toolRegistry = McpToolRegistryProvider.fromTransport(
                transport = transport,
                name = "koog-mcp-process-client",
                version = "1.0.0"
            )

            logger.info { "Connected to MCP process. Available tools: ${toolRegistry.tools.keys}" }

            // エージェントの作成と実行
            val agent = createAgentWithMcpTools(toolRegistry)
            val result = agent.execute(
                Prompt("Execute a sample MCP tool and show the result")
            )

            logger.info { "Agent response: $result" }

            // プロセスの終了
            process.destroy()

        } catch (e: Exception) {
            logger.error(e) { "Failed to start MCP process" }
        }
    }

    /**
     * MCPツールを使用したエージェントの作成
     */
    private fun createAgentWithMcpTools(toolRegistry: ToolRegistry): AIAgent<StringSubgraphResult> {
        val executor = SimplePromptExecutor(
            apiKey = System.getenv("OPENAI_API_KEY") ?: throw IllegalStateException("OPENAI_API_KEY not set")
        )

        val strategy = agentStrategies {
            graph<StringSubgraphResult> {
                val result = agent {
                    name = "mcp-agent"
                    description = "Agent with MCP tools integration"
                    prompt = "You are an AI assistant with access to MCP tools. Use them to help users."
                    outputSchema = StringSubgraphResult::class
                }
                result
            }
        }

        return AIAgent(
            executor = executor,
            strategy = strategy,
            llmModel = OpenAIModels.Chat.GPT4o,
            toolRegistry = toolRegistry
        )
    }

    /**
     * MCPツールの詳細情報を取得
     */
    suspend fun inspectMcpTools() {
        logger.info { "Inspecting MCP tools..." }

        val transport = McpToolRegistryProvider.defaultSseTransport(
            url = "http://localhost:8931/sse"
        )

        val toolRegistry = McpToolRegistryProvider.fromTransport(
            transport = transport,
            name = "koog-inspector",
            version = "1.0.0"
        )

        toolRegistry.tools.forEach { (name, tool) ->
            logger.info {
                """
                Tool: $name
                Description: ${tool.description}
                Parameters: ${tool.inputSchema}
                """.trimIndent()
            }
        }
    }
}

/**
 * メイン実行関数
 */
fun main() = runBlocking {
    val example = McpBasicExample()

    // SSE接続の例を実行
    example.runSseExample()

    // ツール情報の確認
    example.inspectMcpTools()
}