package com.koog.examples.phase2.config

import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.llm.LLModel
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "phase2")
data class Phase2Config(
    val model: String = "gemini-2.0-flash-001",
    val systemPrompt: String = "あなたは様々なツールを使いこなす有能なアシスタントです。ユーザーの要求に応じて適切なツールを選択し、実行してください。",
    val maxTokens: Int = 2048,
    val temperature: Double = 0.7
) {
    val llmModel: LLModel
        get() = when (model) {
            "gemini-2.0-flash-001" -> GoogleModels.Gemini2_0Flash001
            "gemini-2.0-flash" -> GoogleModels.Gemini2_0Flash
            "gemini-2.5-flash" -> GoogleModels.Gemini2_5Flash
            else -> GoogleModels.Gemini2_0Flash001
        }
}
