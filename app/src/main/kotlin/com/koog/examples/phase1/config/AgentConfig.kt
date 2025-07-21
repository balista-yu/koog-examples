package com.koog.examples.phase1.config

import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.llm.LLModel
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "agent")
data class AgentConfig(
    val apiKey: String = "",
    val model: String = "gemini-2.0-flash-001",
    val systemPrompt: String = "You are a helpful assistant. Please respond in Japanese."
) {
    val llmModel: LLModel
        get() = when (model) {
            "gemini-1.5-flash" -> GoogleModels.Gemini1_5Flash
            "gemini-1.5-pro" -> GoogleModels.Gemini1_5Pro
            "gemini-2.0-flash-001" -> GoogleModels.Gemini2_0Flash001
            "gemini-2.0-flash" -> GoogleModels.Gemini2_0Flash
            else -> GoogleModels.Gemini1_5Flash
        }
}
