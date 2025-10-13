package com.koog.examples.phase4.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.koog.examples.phase4.config.Phase4Config
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ChromeDevToolsMcpService(
    @Value("\${api.google-api-key}") private val googleApiKey: String,
    private val config: Phase4Config
) {
    private val logger = KotlinLogging.logger {}
    private var mcpProcess: Process? = null
    private var toolRegistry: ToolRegistry? = null
    private var agent: AIAgent<String, String>? = null

    @PostConstruct
    fun init() {
        try {
            logger.info { "Starting Chrome DevTools MCP Server..." }

            val headlessFlag = if (config.chromeDevtools.headless) "--headless" else ""
            mcpProcess = ProcessBuilder(
                "npx", "-y", "chrome-devtools-mcp@latest",
                headlessFlag
            ).apply {
                redirectError(ProcessBuilder.Redirect.DISCARD)
            }.start()

            Thread.sleep(config.chromeDevtools.startupTimeout)
            logger.info { "Connecting to Chrome DevTools MCP Server..." }

            try {
                toolRegistry = runBlocking {
                    McpToolRegistryProvider.fromTransport(
                        transport = McpToolRegistryProvider.defaultStdioTransport(mcpProcess!!)
                    )
                }
                logger.info { "MCP connection established successfully" }
            } catch (mcpError: Exception) {
                logger.warn { "MCP connection failed: ${mcpError.message}" }
                logger.warn { "Continuing without MCP tools support" }
            }

        } catch (e: Exception) {
            logger.error(e) { "Failed to start Chrome DevTools MCP Server" }
            cleanup()
            logger.warn { "Continuing without Chrome DevTools MCP support" }
        }

        try {
            agent = if (toolRegistry != null) {
                AIAgent(
                    executor = simpleGoogleAIExecutor(googleApiKey),
                    llmModel = GoogleModels.Gemini2_0Flash001,
                    toolRegistry = toolRegistry!!,
                    systemPrompt = config.chromeDevtools.systemPrompt
                )
            } else {
                AIAgent(
                    executor = simpleGoogleAIExecutor(googleApiKey),
                    llmModel = GoogleModels.Gemini2_0Flash001,
                    systemPrompt = config.chromeDevtools.fallbackSystemPrompt
                )
            }
            logger.info { "Chrome DevTools service initialized ${if (toolRegistry != null) "with" else "without"} MCP capabilities" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize AI agent" }
            throw e
        }
    }

    @PreDestroy
    fun cleanup() {
        logger.info { "Cleaning up Chrome DevTools MCP Service..." }
        mcpProcess?.let {
            if (it.isAlive) {
                it.destroyForcibly()
                logger.info { "Chrome DevTools MCP Server stopped" }
            }
        }
        mcpProcess = null
        toolRegistry = null
        agent = null
    }

    fun executeTask(task: String): String {
        if (agent == null) {
            return "Chrome DevTools MCP Server is not available"
        }

        return try {
            logger.info { "Executing task: $task" }
            runBlocking {
                agent!!.run(task)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute task: $task" }
            "Error executing task: ${e.message}"
        }
    }

    fun getAvailableTools(): List<String> {
        return toolRegistry?.tools?.map { it.name } ?: emptyList()
    }

    fun isReady(): Boolean = agent != null

    fun getStatus(): Map<String, Any> {
        val availableTools = getAvailableTools()
        return mapOf(
            "ready" to isReady(),
            "processAlive" to (mcpProcess?.isAlive ?: false),
            "toolsCount" to availableTools.size,
            "availableTools" to availableTools
        )
    }
}
