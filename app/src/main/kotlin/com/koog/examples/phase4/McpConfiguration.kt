package com.koog.examples.phase4

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Phase 4: MCPシミュレーションの設定クラス
 */
@Configuration
class McpConfiguration {

    @Bean
    fun mcpSimulationExample(): McpSimulationSimple {
        return McpSimulationSimple()
    }
}