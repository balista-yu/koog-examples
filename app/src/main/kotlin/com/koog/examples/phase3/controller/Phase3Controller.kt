package com.koog.examples.phase3.controller

import com.koog.examples.phase3.agent.KoogEventHandlerAgent
import com.koog.examples.phase3.agent.SimpleEventAgent
import com.koog.examples.phase3.agent.SubgraphAgent
import com.koog.examples.phase3.dto.*
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/phase3")
class Phase3Controller(
    private val koogEventHandlerAgent: KoogEventHandlerAgent,
    private val simpleEventAgent: SimpleEventAgent,
    private val subgraphAgent: SubgraphAgent
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/simple-events")
    suspend fun processSimpleEvents(
        @RequestBody request: EventDrivenRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Processing with simple Koog events: ${request.message}")

        val result = simpleEventAgent.processWithEvents(request.message)

        return ResponseEntity.ok(
            mapOf(
                "success" to result.success,
                "message" to result.message,
                "events" to result.events,
                "duration" to result.duration
            )
        )
    }

    @PostMapping("/detailed-events")
    suspend fun processDetailedEvents(
        @RequestBody request: EventDrivenRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Processing with detailed Koog events: ${request.message}")

        val result = koogEventHandlerAgent.processWithDetailedEvents(request.message)

        return ResponseEntity.ok(
            mapOf(
                "success" to result.success,
                "sessionId" to result.sessionId,
                "message" to result.message,
                "events" to result.events.map { event ->
                    mapOf(
                        "timestamp" to event.timestamp.toString(),
                        "type" to event.type.toString(),
                        "details" to event.details
                    )
                },
                "metrics" to result.metrics
            )
        )
    }

    @PostMapping("/parallel-events")
    suspend fun processParallelEvents(
        @RequestBody request: ParallelRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Processing parallel tasks with events: ${request.tasks.size} tasks")

        val result = koogEventHandlerAgent.processParallelWithEvents(request.tasks)

        return ResponseEntity.ok(
            mapOf(
                "sessionId" to result.sessionId,
                "totalTasks" to result.totalTasks,
                "completedTasks" to result.completedTasks,
                "results" to result.results.map { task ->
                    mapOf(
                        "taskId" to task.taskId,
                        "task" to task.task,
                        "result" to task.result,
                        "duration" to task.duration,
                        "toolCallCount" to task.toolCallCount
                    )
                },
                "totalDuration" to result.totalDuration,
                "averageTaskDuration" to result.averageTaskDuration
            )
        )
    }


    @PostMapping("/subgraph")
    suspend fun processWithSubgraph(
        @RequestBody request: EventDrivenRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Processing with Koog subgraph: ${request.message}")

        val result = subgraphAgent.processWithSubgraph(request.message)

        return ResponseEntity.ok(
            mapOf(
                "success" to result.success,
                "sessionId" to result.sessionId,
                "analysisResult" to result.analysisResult.result,
                "processingResult" to result.processingResult.result,
                "finalResult" to result.finalResult.result,
                "duration" to result.duration
            )
        )
    }

    @PostMapping("/parallel-subgraphs")
    suspend fun processParallelSubgraphs(
        @RequestBody request: ParallelRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Processing parallel subgraphs: ${request.tasks.size} tasks")

        val result = subgraphAgent.processParallelSubgraphs(request.tasks)

        return ResponseEntity.ok(
            mapOf(
                "sessionId" to result.sessionId,
                "totalTasks" to result.totalTasks,
                "taskResults" to result.taskResults.map { task ->
                    mapOf(
                        "taskId" to task.taskId,
                        "taskIndex" to task.taskIndex,
                        "task" to task.task,
                        "result" to task.result.result,
                        "duration" to task.duration
                    )
                },
                "aggregatedResult" to result.aggregatedResult.result,
                "totalDuration" to result.totalDuration
            )
        )
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "version" to "2.0",
                "features" to listOf(
                    "Koog EventHandler Integration",
                    "Koog StringSubgraphResult Support",
                    "Simple Event Processing",
                    "Detailed Event Tracking",
                    "Parallel Task Processing",
                    "Subgraph Processing Pipeline"
                ),
                "endpoints" to mapOf(
                    "POST /api/phase3/simple-events" to "Simple event processing",
                    "POST /api/phase3/detailed-events" to "Detailed event tracking",
                    "POST /api/phase3/parallel-events" to "Parallel task processing",
                    "POST /api/phase3/subgraph" to "Subgraph pipeline processing",
                    "POST /api/phase3/parallel-subgraphs" to "Parallel subgraph execution"
                )
            )
        )
    }
}
