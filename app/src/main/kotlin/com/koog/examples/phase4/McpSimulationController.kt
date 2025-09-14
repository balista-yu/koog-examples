package com.koog.examples.phase4

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

/**
 * Phase 4: MCPシミュレーションのREST APIコントローラー
 */
@RestController
@RequestMapping("/api/phase4/mcp")
class McpSimulationController(
    private val mcpSimulationExample: McpSimulationSimple
) {

    /**
     * MCPシミュレーションのステータス確認
     */
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "phase" to "4",
            "title" to "Model Context Protocol (MCP) Simulation",
            "description" to "MCP functionality simulation using Koog 0.4.1 custom tools",
            "note" to "Official MCP support will be added in future Koog releases",
            "components" to listOf(
                "Custom Tool Registry (MCP simulation)",
                "Echo Tool",
                "Time Tool",
                "Calculator Tool",
                "Data Fetch Tool"
            ),
            "status" to "active"
        ))
    }

    /**
     * シミュレートされたツールの一覧取得
     */
    @GetMapping("/tools")
    fun listTools(): ResponseEntity<Map<String, Any>> {
        logger.info { "Listing simulated MCP tools..." }

        mcpSimulationExample.displayToolInfo()

        val tools = listOf(
            mapOf(
                "name" to "echo",
                "description" to "Echo the input message",
                "inputSchema" to "EchoInput(message: String)",
                "outputSchema" to "EchoOutput(response: String)"
            ),
            mapOf(
                "name" to "get_time",
                "description" to "Get current time in various formats",
                "inputSchema" to "TimeInput(format: String)",
                "outputSchema" to "TimeOutput(time: String, format: String)"
            ),
            mapOf(
                "name" to "calculator",
                "description" to "Perform basic mathematical calculations",
                "inputSchema" to "CalculatorInput(operation: String, a: Double, b: Double)",
                "outputSchema" to "CalculatorOutput(result: Double, expression: String)"
            ),
            mapOf(
                "name" to "data_fetch",
                "description" to "Fetch data from simulated external source",
                "inputSchema" to "DataFetchInput(query: String)",
                "outputSchema" to "DataFetchOutput(data: String, source: String)"
            )
        )

        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "tools" to tools,
            "count" to tools.size
        ))
    }

    /**
     * MCPシミュレーションの実行
     */
    @PostMapping("/simulate")
    fun runSimulation(): ResponseEntity<Map<String, Any>> {
        logger.info { "Running MCP simulation..." }

        return try {
            runBlocking {
                mcpSimulationExample.runMcpSimulation()
            }
            ResponseEntity.ok(mapOf(
                "status" to "success",
                "message" to "MCP simulation completed successfully. Check logs for details."
            ))
        } catch (e: Exception) {
            logger.error(e) { "MCP simulation failed" }
            ResponseEntity.ok(mapOf(
                "status" to "error",
                "message" to (e.message ?: "Unknown error")
            ))
        }
    }

    /**
     * エコーツールのテスト
     */
    @PostMapping("/tools/echo")
    fun testEchoTool(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val message = request["message"] ?: "Hello, MCP!"
        logger.info { "Testing echo tool with message: $message" }

        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "tool" to "echo",
            "input" to message,
            "output" to "Echo: $message"
        ))
    }

    /**
     * 時刻取得ツールのテスト
     */
    @GetMapping("/tools/time")
    fun testTimeTool(@RequestParam(defaultValue = "iso") format: String): ResponseEntity<Map<String, Any>> {
        logger.info { "Testing time tool with format: $format" }

        val time = when (format) {
            "unix" -> System.currentTimeMillis() / 1000
            "millis" -> System.currentTimeMillis()
            else -> java.time.Instant.now().toString()
        }

        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "tool" to "get_time",
            "format" to format,
            "time" to time
        ))
    }

    /**
     * 計算ツールのテスト
     */
    @PostMapping("/tools/calculate")
    fun testCalculatorTool(@RequestBody request: CalculationRequest): ResponseEntity<Map<String, Any>> {
        logger.info { "Testing calculator tool: ${request.a} ${request.operation} ${request.b}" }

        val result = when (request.operation) {
            "add" -> request.a + request.b
            "subtract" -> request.a - request.b
            "multiply" -> request.a * request.b
            "divide" -> if (request.b != 0.0) request.a / request.b else Double.NaN
            else -> Double.NaN
        }

        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "tool" to "calculator",
            "expression" to "${request.a} ${request.operation} ${request.b}",
            "result" to result
        ))
    }

    /**
     * データ取得ツールのテスト
     */
    @GetMapping("/tools/data")
    fun testDataFetchTool(@RequestParam query: String): ResponseEntity<Map<String, Any>> {
        logger.info { "Testing data fetch tool with query: $query" }

        val mockData = mapOf(
            "weather" to "Sunny, 22°C",
            "stock" to "AAPL: $150.00 (+1.2%)",
            "news" to "Latest: AI advances continue to reshape technology"
        )

        val result = mockData[query.lowercase()] ?: "No data available for: $query"

        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "tool" to "data_fetch",
            "query" to query,
            "data" to result
        ))
    }
}

/**
 * 計算リクエスト
 */
data class CalculationRequest(
    val operation: String,
    val a: Double,
    val b: Double
)