package com.koog.examples.phase1.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.koog.examples.phase1.config.AgentConfig
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ChatAgent(
    private val config: AgentConfig,
    @Value("\${api.google-api-key}")
    private val googleApiKey: String,
) {

    suspend fun processMessage(message: String): String {
        logger.info { "Processing message: $message" }
        logger.info { "Using model: ${config.model}" }

        return try {
            // AIエージェントの作成
            val agent = AIAgent(
                executor = simpleGoogleAIExecutor(googleApiKey),
                systemPrompt = config.systemPrompt,
                llmModel = config.llmModel,
                temperature = 0.7
            )

            val result = agent.run(message)
            logger.info { "Agent response: $result" }
            result
        } catch (e: Exception) {
            logger.error(e) { "Error processing message" }
            "Error: ${e.message}"
        }
    }
}
