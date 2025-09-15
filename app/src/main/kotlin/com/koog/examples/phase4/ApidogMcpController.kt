package com.koog.examples.phase4

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/phase4/apidog")
class ApidogMcpController(
    private val apidogMcpService: ApidogMcpService
) {

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "phase" to "4",
            "service" to "Apidog MCP Integration",
            "description" to "Real MCP integration with Apidog API documentation service",
            "configured" to apidogMcpService.isConfigured(),
            "capabilities" to listOf(
                "API Specification Retrieval",
                "API Testing",
                "Documentation Generation",
                "OpenAPI/Swagger Support"
            )
        ))
    }

    @PostMapping("/query")
    fun executeQuery(@RequestBody request: ApidogQueryRequest): ResponseEntity<Map<String, Any>> {
        logger.info { "Executing Apidog query: ${request.query}" }

        val result = apidogMcpService.executeQuery(request.query)

        return ResponseEntity.ok(mapOf(
            "query" to request.query,
            "result" to result,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    @GetMapping("/api-spec/{apiName}")
    fun getApiSpecification(@PathVariable apiName: String): ResponseEntity<Map<String, Any>> {
        logger.info { "Retrieving API specification for: $apiName" }

        val specification = apidogMcpService.getApiSpecification(apiName)

        return ResponseEntity.ok(specification)
    }

    @PostMapping("/test-endpoint")
    fun testApiEndpoint(@RequestBody request: TestEndpointRequest): ResponseEntity<Map<String, Any>> {
        logger.info { "Testing API endpoint: ${request.endpoint} with method: ${request.method}" }

        val result = apidogMcpService.testApiEndpoint(
            endpoint = request.endpoint,
            method = request.method,
            body = request.body
        )

        return ResponseEntity.ok(result)
    }

    @GetMapping("/examples")
    fun getExamples(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "description" to "Example queries for Apidog MCP integration",
            "examples" to listOf(
                mapOf(
                    "type" to "Get API Specification",
                    "query" to "Get the complete API specification for the user service",
                    "endpoint" to "/phase4/apidog/api-spec/user-service"
                ),
                mapOf(
                    "type" to "Test API Endpoint",
                    "query" to "Test the POST /users endpoint with sample data",
                    "endpoint" to "/phase4/apidog/test-endpoint",
                    "body" to mapOf(
                        "endpoint" to "/users",
                        "method" to "POST",
                        "body" to """{"name": "John Doe", "email": "john@example.com"}"""
                    )
                ),
                mapOf(
                    "type" to "General Query",
                    "query" to "Show me all available endpoints in the payment service",
                    "endpoint" to "/phase4/apidog/query"
                ),
                mapOf(
                    "type" to "Documentation Query",
                    "query" to "Generate API documentation for the authentication endpoints",
                    "endpoint" to "/phase4/apidog/query"
                )
            ),
            "note" to "Make sure APIDOG_ACCESS_TOKEN and APIDOG_PROJECT_ID are configured"
        ))
    }
}

data class ApidogQueryRequest(
    val query: String
)

data class TestEndpointRequest(
    val endpoint: String,
    val method: String,
    val body: String? = null
)