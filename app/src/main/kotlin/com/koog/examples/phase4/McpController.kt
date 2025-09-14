package com.koog.examples.phase4

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

/**
 * Phase 4: MCP統合のREST APIコントローラー
 */
@RestController
@RequestMapping("/api/phase4/mcp")
class McpController(
    private val mcpBasicExample: McpBasicExample,
    private val mcpToolIntegration: McpToolIntegration,
    private val mcpResourceManager: McpResourceManager,
    private val mcpPromptStrategy: McpPromptStrategy
) {

    /**
     * MCP接続テスト
     */
    @GetMapping("/test-connection")
    fun testConnection(): ResponseEntity<Map<String, Any>> {
        logger.info { "Testing MCP connection..." }

        return try {
            runBlocking {
                mcpBasicExample.inspectMcpTools()
            }
            ResponseEntity.ok(mapOf(
                "status" to "success",
                "message" to "MCP connection test completed"
            ))
        } catch (e: Exception) {
            logger.error(e) { "MCP connection test failed" }
            ResponseEntity.ok(mapOf(
                "status" to "error",
                "message" to e.message
            ))
        }
    }

    /**
     * MCPツールの一覧取得
     */
    @GetMapping("/tools")
    fun listTools(): ResponseEntity<Map<String, Any>> {
        logger.info { "Listing MCP tools..." }

        return try {
            runBlocking {
                val registry = mcpToolIntegration.createHybridToolRegistry()
                val tools = registry.tools.map { (name, tool) ->
                    mapOf(
                        "name" to name,
                        "description" to tool.description
                    )
                }

                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "tools" to tools,
                    "count" to tools.size
                ))
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to list tools" }
            ResponseEntity.ok(mapOf(
                "status" to "error",
                "message" to e.message
            ))
        }
    }

    /**
     * MCPツールの実行
     */
    @PostMapping("/tools/execute")
    fun executeTool(@RequestBody request: ToolExecutionRequest): ResponseEntity<Map<String, Any>> {
        logger.info { "Executing tool: ${request.toolName}" }

        return try {
            runBlocking {
                mcpToolIntegration.executeMcpTool()
            }
            ResponseEntity.ok(mapOf(
                "status" to "success",
                "message" to "Tool execution completed"
            ))
        } catch (e: Exception) {
            logger.error(e) { "Tool execution failed" }
            ResponseEntity.ok(mapOf(
                "status" to "error",
                "message" to e.message
            ))
        }
    }

    /**
     * リソースの取得
     */
    @GetMapping("/resources/{resourceId}")
    fun getResource(@PathVariable resourceId: String): ResponseEntity<Map<String, Any>> {
        logger.info { "Getting resource: $resourceId" }

        val resource = mcpResourceManager.getResource(resourceId)

        return if (resource != null) {
            ResponseEntity.ok(mapOf(
                "status" to "success",
                "resource" to mapOf(
                    "id" to resource.id,
                    "name" to resource.name,
                    "type" to resource.type.name,
                    "content" to resource.content,
                    "metadata" to resource.metadata
                )
            ))
        } else {
            ResponseEntity.ok(mapOf(
                "status" to "not_found",
                "message" to "Resource not found: $resourceId"
            ))
        }
    }

    /**
     * プロンプト生成
     */
    @PostMapping("/prompts/generate")
    fun generatePrompt(@RequestBody request: PromptGenerationRequest): ResponseEntity<Map<String, Any>> {
        logger.info { "Generating prompt with template: ${request.templateId}" }

        return try {
            val prompt = mcpPromptStrategy.generatePrompt(
                templateId = request.templateId,
                variables = request.variables
            )

            ResponseEntity.ok(mapOf(
                "status" to "success",
                "prompt" to prompt
            ))
        } catch (e: Exception) {
            logger.error(e) { "Prompt generation failed" }
            ResponseEntity.ok(mapOf(
                "status" to "error",
                "message" to e.message
            ))
        }
    }

    /**
     * エージェント実行
     */
    @PostMapping("/agent/execute")
    fun executeAgent(@RequestBody request: AgentExecutionRequest): ResponseEntity<Map<String, Any>> {
        logger.info { "Executing agent with prompt: ${request.prompt.take(50)}..." }

        return try {
            runBlocking {
                when (request.type) {
                    "basic" -> mcpBasicExample.runSseExample()
                    "hybrid" -> mcpToolIntegration.runHybridAgent()
                    "resource" -> mcpResourceManager.runResourceAwareAgent()
                    "prompt" -> mcpPromptStrategy.runDynamicPromptStrategy()
                    else -> throw IllegalArgumentException("Unknown agent type: ${request.type}")
                }
            }

            ResponseEntity.ok(mapOf(
                "status" to "success",
                "message" to "Agent execution completed"
            ))
        } catch (e: Exception) {
            logger.error(e) { "Agent execution failed" }
            ResponseEntity.ok(mapOf(
                "status" to "error",
                "message" to e.message
            ))
        }
    }

    /**
     * Phase 4のステータス確認
     */
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "phase" to "4",
            "title" to "Model Context Protocol (MCP) Integration",
            "components" to listOf(
                "Basic MCP Integration",
                "Tool Integration",
                "Resource Management",
                "Prompt Strategy"
            ),
            "status" to "active"
        ))
    }
}

/**
 * ツール実行リクエスト
 */
data class ToolExecutionRequest(
    val toolName: String,
    val input: Map<String, Any>
)

/**
 * プロンプト生成リクエスト
 */
data class PromptGenerationRequest(
    val templateId: String,
    val variables: Map<String, Any>
)

/**
 * エージェント実行リクエスト
 */
data class AgentExecutionRequest(
    val type: String,
    val prompt: String,
    val parameters: Map<String, Any> = emptyMap()
)