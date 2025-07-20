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
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@RestController
@RequestMapping("/api/agents")
class AgentController(
    private val helloWorldAgent: HelloWorldAgent,
    private val chatAgent: ChatAgent,
    @Value("\${ai.koog.google.api-key:not-set}") private val googleApiKey: String,
    @Autowired private val applicationContext: ApplicationContext
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
    
    @GetMapping("/config")
    fun config(): Map<String, Any> {
        val executorBeans = applicationContext.getBeanNamesForType(ai.koog.prompt.executor.llms.SingleLLMPromptExecutor::class.java)
        return mapOf(
            "googleApiKey" to if (googleApiKey == "your-api-key" || googleApiKey == "not-set") "NOT_SET" else "SET (${googleApiKey.take(10)}...)",
            "envGoogleApiKey" to if (System.getenv("GOOGLE_API_KEY").isNullOrEmpty()) "NOT_SET" else "SET",
            "springProfile" to (System.getProperty("spring.profiles.active") ?: "default"),
            "apiKeyLength" to googleApiKey.length,
            "apiKeyStartsWith" to googleApiKey.take(7),
            "executorBeans" to executorBeans.toList()
        )
    }
}
