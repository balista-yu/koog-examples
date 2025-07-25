package com.koog.examples.phase1.config

import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.llm.LLModel
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "agent")
data class AgentConfig(
    val model: String = "gemini-2.0-flash-001",
    val systemPrompt: String = "You are a helpful assistant. Please respond in Japanese."
) {
    val llmModel: LLModel
        get() = when (model) {
            "gemini-2.0-flash-001" -> GoogleModels.Gemini2_0Flash001
            "gemini-2.0-flash" -> GoogleModels.Gemini2_0Flash
            "gemini-2.5-flash" -> GoogleModels.Gemini2_5Flash
            else -> GoogleModels.Gemini2_0Flash001
        }
}
