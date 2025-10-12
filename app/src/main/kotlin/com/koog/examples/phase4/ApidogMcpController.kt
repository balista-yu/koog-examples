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
}

data class ApidogQueryRequest(
    val query: String
)
