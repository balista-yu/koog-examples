package com.koog.examples.phase4

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Phase 4: MCP統合の設定クラス
 */
@Configuration
class McpConfiguration {

    @Bean
    fun mcpBasicExample(): McpBasicExample {
        return McpBasicExample()
    }

    @Bean
    fun mcpToolIntegration(): McpToolIntegration {
        return McpToolIntegration()
    }

    @Bean
    fun mcpResourceManager(): McpResourceManager {
        return McpResourceManager()
    }

    @Bean
    fun mcpPromptStrategy(): McpPromptStrategy {
        return McpPromptStrategy()
    }
}