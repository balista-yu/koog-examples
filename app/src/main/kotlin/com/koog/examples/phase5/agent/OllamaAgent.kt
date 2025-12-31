package com.koog.examples.phase5.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.singleRunStrategy
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import com.koog.examples.phase5.config.Phase5Config
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class OllamaAgent(
    private val config: Phase5Config,
    @Qualifier("ollamaExecutor")
    private val promptExecutor: PromptExecutor
) {
    private val aiAgentConfig: AIAgentConfig by lazy {
        AIAgentConfig(
            prompt = prompt("ollama-agent") {
                system(config.systemPrompt)
            },
            model = config.llmModel,
            maxAgentIterations = config.maxIterations
        )
    }

    suspend fun chat(userMessage: String): String {
        logger.info { "Processing user message with Ollama: $userMessage" }

        return try {
            val agent = AIAgent(
                promptExecutor = promptExecutor,
                strategy = singleRunStrategy(),
                agentConfig = aiAgentConfig
            )

            val response = agent.run(userMessage)
            logger.info { "Ollama response generated successfully" }
            response
        } catch (e: Exception) {
            logger.error(e) { "Error processing message with Ollama" }
            "エラーが発生しました: ${e.message}"
        }
    }
}
