package com.koog.examples.phase1.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.koog.examples.phase1.config.AgentConfig
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ChatAgent(
    private val config: AgentConfig
) {

    suspend fun processMessage(message: String): String {
        logger.info { "Processing message: $message" }
        logger.info { "Using model: ${config.model}" }

        // AIエージェントの作成
        val agent = AIAgent(
            executor = simpleGoogleAIExecutor(config.apiKey),
            systemPrompt = config.systemPrompt,
            llmModel = config.llmModel,
            temperature = 0.7,
        )

        val result = agent.run(message)
        logger.info { "Agent response: $result" }

        return result.orEmpty()
    }
}
