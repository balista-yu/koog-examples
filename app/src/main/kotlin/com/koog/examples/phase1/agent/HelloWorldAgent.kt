package com.koog.examples.phase1.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.koog.examples.phase1.config.AgentConfig
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class HelloWorldAgent(
    private val config: AgentConfig
) {

    suspend fun runExample(): String {
        logger.info { "Starting Hello World Agent example..." }
        logger.info { "Using model: ${config.model}" }

        // AIエージェントの作成
        val agent = AIAgent(
            executor = simpleGoogleAIExecutor(config.apiKey),
            systemPrompt = config.systemPrompt,
            llmModel = config.llmModel
        )

        // 基本的な対話
        val userMessage = "Hello! How can you help me?"
        logger.info { "Sending message: $userMessage" }

        val result = agent.run(userMessage)
        logger.info { "Agent response: $result" }

        return result.orEmpty()
    }
}
