package com.koog.examples.phase3.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "phase3")
data class Phase3Config(
    var llmModel: String = "gemini-2.0-flash-exp",
    var systemPrompt: String = """
        あなたは高度なエージェント戦略を持つAIアシスタントです。
        イベント駆動型の処理とカスタム戦略グラフを使用して、
        効率的にタスクを実行します。
    """.trimIndent(),
    var temperature: Double = 0.7,
    var maxIterations: Int = 15,
    var enableEventLogging: Boolean = true,
    var enableParallelExecution: Boolean = true,
    var historyCompressionThreshold: Int = 10
)
