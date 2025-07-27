package com.koog.examples.phase2.dto

import com.koog.examples.phase2.agent.ToolAgent

data class ToolResponse(
    val response: String,
)

data class ToolsInfoResponse(
    val availableTools: List<ToolAgent.ToolInfo>
)
