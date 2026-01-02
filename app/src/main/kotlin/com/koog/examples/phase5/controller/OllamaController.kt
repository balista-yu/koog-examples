package com.koog.examples.phase5.controller

import com.koog.examples.phase5.agent.OllamaAgent
import com.koog.examples.phase5.config.Phase5Config
import com.koog.examples.phase5.dto.OllamaRequest
import com.koog.examples.phase5.dto.OllamaResponse
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/phase5/ollama")
class OllamaController(
    private val ollamaAgent: OllamaAgent,
    private val config: Phase5Config
) {
    @PostMapping("/chat")
    fun chat(@RequestBody request: OllamaRequest): ResponseEntity<OllamaResponse> = runBlocking {
        logger.info { "Received Ollama chat request: ${request.message}" }

        return@runBlocking try {
            val response = ollamaAgent.chat(request.message)
            ResponseEntity.ok(
                OllamaResponse(
                    message = request.message,
                    response = response,
                    model = config.llmModel.id,
                    success = true
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Error processing Ollama chat request" }
            ResponseEntity.internalServerError().body(
                OllamaResponse(
                    message = request.message,
                    response = "エラーが発生しました: ${e.message}",
                    model = config.llmModel.id,
                    success = false
                )
            )
        }
    }

    @GetMapping("/info")
    fun info(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "phase" to "Phase 5: Ollama Integration",
                "model" to config.llmModel.id,
                "maxIterations" to config.maxIterations,
                "endpoints" to mapOf(
                    "POST /api/phase5/ollama/chat" to "Chat with Ollama model"
                )
            )
        )
    }
}
