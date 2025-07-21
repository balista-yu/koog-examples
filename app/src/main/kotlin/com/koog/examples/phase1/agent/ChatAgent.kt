package com.koog.examples.phase1.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.koog.examples.phase1.config.AgentConfig
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ChatAgent(
    private val config: AgentConfig
) {

    suspend fun processMessage(message: String): String {
        logger.info { "Processing message: $message" }
        logger.info { "Using model: ${config.model}" }

        return try {
            // AIエージェントの作成
            val agent = AIAgent(
                executor = simpleGoogleAIExecutor(config.apiKey),
                systemPrompt = config.systemPrompt,
                llmModel = config.llmModel,
                temperature = 0.7
            )

            val result = agent.run(message)
            logger.info { "Agent response: $result" }
            result.orEmpty()
        } catch (e: Exception) {
            logger.error(e) { "Error processing message" }
            "Error: ${e.message}"
        }
    }
}
