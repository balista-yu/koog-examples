package com.koog.examples.phase3.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.clients.google.GoogleModels
import com.koog.examples.phase3.config.Phase3Config
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * シンプルなKoog EventHandler実装例
 * Koogの標準EventHandler機能のみを使用
 */
@Component
class SimpleEventAgent(
    private val config: Phase3Config,
    @param:Value("\${api.google-api-key}")
    private val googleApiKey: String
) {
    private val logger = KotlinLogging.logger {}

    private val toolRegistry = ToolRegistry {
        tool(AskUser)
        tool(SayToUser)
    }

    private val executor = simpleGoogleAIExecutor(googleApiKey)

    /**
     * Koog EventHandlerを使用したシンプルな処理
     */
    suspend fun processWithEvents(userMessage: String): SimpleEventResponse {
        val startTime = System.currentTimeMillis()
        val events = mutableListOf<String>()

        try {
            logger.info { "Processing message: $userMessage" }
            events.add("${LocalDateTime.now()}: Processing started")

            val agent = AIAgent(
                llmModel = GoogleModels.Gemini2_0Flash001,
                executor = executor,
                systemPrompt = config.systemPrompt,
                temperature = config.temperature,
                toolRegistry = toolRegistry,
                maxIterations = config.maxIterations
            ) {
                // Koog標準のEventHandler機能
                handleEvents {
                    onToolCall { eventContext ->
                        val eventMsg = "${LocalDateTime.now()}: Tool called: ${eventContext.tool.name}"
                        events.add(eventMsg)
                        logger.info { eventMsg }
                    }

                    onAgentFinished { eventContext ->
                        val eventMsg = "${LocalDateTime.now()}: Agent finished"
                        events.add(eventMsg)
                        logger.info { eventMsg }
                    }
                }
            }

            val response = agent.run(userMessage)
            val duration = System.currentTimeMillis() - startTime

            events.add("${LocalDateTime.now()}: Processing completed in ${duration}ms")

            return SimpleEventResponse(
                message = response,
                events = events,
                duration = duration,
                success = true
            )
        } catch (e: Exception) {
            logger.error(e) { "Error during processing" }
            events.add("${LocalDateTime.now()}: Error: ${e.message}")

            return SimpleEventResponse(
                message = "エラーが発生しました: ${e.message}",
                events = events,
                duration = System.currentTimeMillis() - startTime,
                success = false
            )
        }
    }
}

data class SimpleEventResponse(
    val message: String,
    val events: List<String>,
    val duration: Long,
    val success: Boolean
)
