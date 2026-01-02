package com.koog.examples.phase5.config

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "phase5")
data class Phase5Config(
    var model: String = "qwen3:0.6b",
    var systemPrompt: String = "あなたは親切なアシスタントです。ユーザーの質問に対して正確かつ簡潔に日本語で回答してください。",
    var temperature: Double = 0.7,
    var maxIterations: Int = 10
) {
    val llmModel: LLModel
        get() = createOllamaModel(model)

    private fun createOllamaModel(modelId: String, contextLen: Long = 32_768L): LLModel = LLModel(
        provider = LLMProvider.Ollama,
        id = modelId,
        capabilities = listOf(
            LLMCapability.Completion,
            LLMCapability.Temperature,
            LLMCapability.Schema.JSON.Basic,
            LLMCapability.Tools
        ),
        contextLength = contextLen
    )
}
