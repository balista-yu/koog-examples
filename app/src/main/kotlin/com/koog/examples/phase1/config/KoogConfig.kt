package com.koog.examples.phase1.config

import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

private val logger = KotlinLogging.logger {}

@Configuration
class KoogConfig {

    @Bean("customGoogleExecutor")
    @Primary
    fun customGoogleExecutor(
        @Value("\${GOOGLE_API_KEY}") apiKey: String
    ): SingleLLMPromptExecutor {
        logger.info { "Creating custom GoogleExecutor with API key: ${apiKey.take(10)}..." }
        val client = GoogleLLMClient(apiKey = apiKey)
        return SingleLLMPromptExecutor(client)
    }
}
