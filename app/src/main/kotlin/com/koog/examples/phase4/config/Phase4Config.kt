package com.koog.examples.phase4.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "phase4")
data class Phase4Config(
    var llmModel: String = "gemini-2.0-flash-001",
    var temperature: Double = 0.7,
    var chromeDevtools: ChromeDevtoolsConfig = ChromeDevtoolsConfig()
) {

    data class ChromeDevtoolsConfig(
        var headless: Boolean = true,
        var startupTimeout: Long = 3000,
        var systemPrompt: String = """
            You are a browser automation and debugging assistant with access to Chrome DevTools MCP tools.
            You can:
            - Navigate web pages and interact with elements
            - Analyze performance and network requests
            - Take screenshots and snapshots
            - Execute JavaScript in the browser context
            - Simulate user interactions (click, fill forms, etc.)
            - Debug console messages and errors

            Always provide clear explanations of what you're doing and what you found.
            Respond in Japanese when asked in Japanese.
        """.trimIndent(),
        var fallbackSystemPrompt: String = """
            You are a browser automation and debugging assistant.
            Note: Chrome DevTools MCP tools are currently unavailable, but you can still help with:
            - Understanding browser automation concepts
            - Explaining how to navigate web pages and interact with elements
            - Describing performance analysis techniques
            - Providing guidance on debugging and testing

            Always provide clear explanations and guidance.
            Respond in Japanese when asked in Japanese.
        """.trimIndent()
    )
}
