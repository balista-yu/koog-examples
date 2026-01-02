package com.koog.examples.phase5.config

import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.OllamaModels
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "phase5")
data class Phase5Config(
    var systemPrompt: String = "あなたは親切なアシスタントです。ユーザーの質問に対して正確かつ簡潔に日本語で回答してください。",
    var maxIterations: Int = 10
) {
    val llmModel: LLModel
        get() = OllamaModels.Alibaba.QWEN_3_06B
}
