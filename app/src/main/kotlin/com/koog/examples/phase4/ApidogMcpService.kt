package com.koog.examples.phase4

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.core.tools.ToolRegistry
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

@Service
class ApidogMcpService(
    @Value("\${api.google-api-key}") private val googleApiKey: String,
    @Value("\${apidog.access.token:}") private val apidogAccessToken: String,
    @Value("\${apidog.project.id:}") private val apidogProjectId: String
) {
    private val logger = KotlinLogging.logger {}
    private var mcpProcess: Process? = null
    private var toolRegistry: ToolRegistry? = null
    private var agent: AIAgent<String, String>? = null

    @PostConstruct
    fun init() {
        try {
            logger.info { "Starting Apidog MCP Server..." }

            mcpProcess = ProcessBuilder(
                "npx", "-y", "apidog-mcp-server@latest",
                "--project=$apidogProjectId"
            ).apply {
                environment()["APIDOG_ACCESS_TOKEN"] = apidogAccessToken
            }.start()

            // Wait for the server to start
            Thread.sleep(2000)
            logger.info { "Connecting to Apidog MCP Server..." }

            try {
                toolRegistry = runBlocking {
                    McpToolRegistryProvider.fromTransport(
                        transport = McpToolRegistryProvider.defaultStdioTransport(mcpProcess!!)
                    )
                }
                logger.info { "MCP connection established successfully" }
            } catch (mcpError: Exception) {
                logger.warn { "MCP connection failed: ${mcpError.message}" }
            }

            agent = AIAgent(
                executor = simpleGoogleAIExecutor(googleApiKey),
                llmModel = GoogleModels.Gemini2_0Flash001,
                systemPrompt = """You are an API documentation assistant with access to Apidog project ID: $apidogProjectId.
                |You help users understand and test API specifications.
                |Provide helpful information about API endpoints, request/response formats, and testing strategies.""".trimMargin()
            )

            logger.info { "Apidog service initialized successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to start Apidog MCP Server" }
            cleanup()
        }
    }

    @PreDestroy
    fun cleanup() {
        mcpProcess?.let {
            if (it.isAlive) {
                it.destroyForcibly()
                logger.info { "Apidog MCP Server stopped" }
            }
        }
        mcpProcess = null
        toolRegistry = null
        agent = null
    }

    fun executeQuery(query: String): String {
        if (agent == null) {
            return "Apidog MCP Server is not configured or failed to start"
        }

        return try {
            runBlocking {
                agent!!.run(query)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute query: $query" }
            "Error executing query: ${e.message}"
        }
    }

    fun getApiSpecification(apiName: String): Map<String, Any> {
        if (agent == null) {
            return mapOf("error" to "Apidog MCP Server is not configured")
        }

        return try {
            val result = runBlocking {
                agent!!.run("Get the API specification for: $apiName")
            }
            mapOf("specification" to result)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get API specification for: $apiName" }
            mapOf("error" to "Failed to retrieve API specification: ${e.message}")
        }
    }

    fun testApiEndpoint(endpoint: String, method: String, body: String?): Map<String, Any> {
        if (agent == null) {
            return mapOf("error" to "Apidog MCP Server is not configured")
        }

        return try {
            val query = buildString {
                append("Test the API endpoint: $endpoint with method: $method")
                body?.let { append(" and body: $it") }
            }

            val result = runBlocking {
                agent!!.run(query)
            }

            mapOf(
                "endpoint" to endpoint,
                "method" to method,
                "result" to result
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to test API endpoint: $endpoint" }
            mapOf("error" to "Failed to test endpoint: ${e.message}")
        }
    }

    fun isConfigured(): Boolean = agent != null
}
