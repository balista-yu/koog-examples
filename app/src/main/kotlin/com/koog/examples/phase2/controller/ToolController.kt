package com.koog.examples.phase2.controller

import com.koog.examples.phase2.agent.ToolAgent
import com.koog.examples.phase2.config.ApiConfig
import com.koog.examples.phase2.dto.ToolRequest
import com.koog.examples.phase2.dto.ToolResponse
import com.koog.examples.phase2.dto.ToolsInfoResponse
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/phase2/tools")
class ToolController(
    private val toolAgent: ToolAgent,
    private val apiConfig: ApiConfig
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/chat")
    fun chat(@RequestBody request: ToolRequest): ResponseEntity<ToolResponse> = runBlocking {
        logger.info("Received chat request: ${request.message}")

        return@runBlocking try {
            val response = toolAgent.process(request.message)
            ResponseEntity.ok(ToolResponse(response = response))
        } catch (e: Exception) {
            logger.error("Error processing chat request", e)
            ResponseEntity.internalServerError()
                .body(ToolResponse(response = "エラーが発生しました: ${e.message}"))
        }
    }

    @GetMapping("/info")
    fun getToolsInfo(): ResponseEntity<ToolsInfoResponse> {
        logger.info("Fetching available tools information")

        val toolsInfo = toolAgent.getAvailableTools()
        return ResponseEntity.ok(ToolsInfoResponse(availableTools = toolsInfo))
    }
}
