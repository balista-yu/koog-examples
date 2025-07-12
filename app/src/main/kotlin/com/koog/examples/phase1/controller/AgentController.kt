package com.koog.examples.phase1.controller

import com.koog.examples.phase1.agent.HelloWorldAgent
import com.koog.examples.phase1.agent.ChatAgent
import com.koog.examples.phase1.dto.ChatRequest
import com.koog.examples.phase1.dto.ChatResponse
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agents")
class AgentController(
    private val helloWorldAgent: HelloWorldAgent,
    private val chatAgent: ChatAgent
) {

    @GetMapping("/hello")
    fun runHelloWorldAgent(): Map<String, Any> {
        return try {
            val result = runBlocking { helloWorldAgent.runExample() }
            mapOf(
                "status" to "success",
                "response" to result
            )
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to "Error: ${e.message}"
            )
        }
    }

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ChatResponse {
        return try {
            val response = runBlocking { chatAgent.processMessage(request.message) }
            ChatResponse(
                status = "success",
                message = request.message,
                response = response
            )
        } catch (e: Exception) {
            ChatResponse(
                status = "error",
                message = request.message,
                response = "Error: ${e.message}"
            )
        }
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "ok", "message" to "Agent service is running")
    }
}
