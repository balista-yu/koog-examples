package com.koog.examples.phase3.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.clients.google.GoogleModels
import com.koog.examples.phase3.config.Phase3Config
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class KoogEventHandlerAgent(
    private val config: Phase3Config,
    @param:Value("\${api.google-api-key}")
    private val googleApiKey: String
) {
    private val logger = KotlinLogging.logger {}

    private val toolRegistry = ToolRegistry {
        tool(AskUser)
        tool(SayToUser)
    }

    private val executor = simpleGoogleAIExecutor(googleApiKey)

    data class EventData(
        val timestamp: LocalDateTime,
        val type: EventType,
        val details: Map<String, Any>,
        val sessionId: String
    )

    enum class EventType {
        AGENT_START,
        TOOL_CALL,
        TOOL_RESULT,
        LLM_CALL,
        LLM_RESPONSE,
        AGENT_FINISHED,
        ERROR
    }

    suspend fun processWithDetailedEvents(userMessage: String): DetailedEventResponse {
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        val events = mutableListOf<EventData>()
        val metrics = mutableMapOf<String, Any>()

        try {
            logger.info { "Starting detailed event tracking for session: $sessionId" }

            var toolCallCount = 0
            var llmCallCount = 0

            val agent = AIAgent(
                llmModel = GoogleModels.Gemini2_0Flash001,
                executor = executor,
                systemPrompt = config.systemPrompt,
                temperature = config.temperature,
                toolRegistry = toolRegistry,
                maxIterations = config.maxIterations
            ) {
                handleEvents {
                    onToolCall { eventContext ->
                        toolCallCount++

                        events.add(
                            EventData(
                                LocalDateTime.now(),
                                EventType.TOOL_CALL,
                                mapOf(
                                    "toolName" to eventContext.tool.name,
                                    "toolDescription" to eventContext.tool.descriptor.description,
                                    "arguments" to eventContext.toolArgs.toString(),
                                    "callNumber" to toolCallCount
                                ),
                                sessionId
                            )
                        )

                        logger.info {
                            "🔧 Tool #$toolCallCount: ${eventContext.tool.name} " +
                                "with args: ${eventContext.toolArgs}"
                        }
                    }

                    onAgentFinished { eventContext ->
                        metrics["totalToolCalls"] = toolCallCount
                        metrics["totalLLMCalls"] = llmCallCount
                        metrics["finalResult"] = eventContext.result.toString()

                        events.add(
                            EventData(
                                LocalDateTime.now(),
                                EventType.AGENT_FINISHED,
                                mapOf(
                                    "result" to eventContext.result.toString(),
                                    "metrics" to metrics
                                ),
                                sessionId
                            )
                        )

                        logger.info {
                            "✅ Agent completed: $toolCallCount tools, $llmCallCount LLM calls"
                        }
                    }
                }
            }

            val response = agent.run(userMessage)
            val duration = System.currentTimeMillis() - startTime

            return DetailedEventResponse(
                sessionId = sessionId,
                message = response,
                events = events,
                metrics = mapOf(
                    "duration" to duration,
                    "toolCalls" to toolCallCount,
                    "llmCalls" to llmCallCount,
                    "eventCount" to events.size
                ),
                success = true
            )
        } catch (e: Exception) {
            logger.error(e) { "Error in detailed event processing" }

            return DetailedEventResponse(
                sessionId = sessionId,
                message = "エラーが発生しました: ${e.message}",
                events = events,
                metrics = mapOf("error" to e.javaClass.simpleName),
                success = false
            )
        }
    }

    suspend fun processParallelWithEvents(tasks: List<String>): ParallelEventResponse = coroutineScope {
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        val allEvents = mutableListOf<EventData>()

        logger.info { "Starting parallel processing with events for ${tasks.size} tasks" }

        try {
            val results = tasks.mapIndexed { index, task ->
                async {
                    val taskId = UUID.randomUUID().toString()
                    val taskStartTime = System.currentTimeMillis()
                    var taskToolCalls = 0

                    val agent = AIAgent(
                        llmModel = GoogleModels.Gemini2_0Flash001,
                        executor = executor,
                        systemPrompt = config.systemPrompt,
                        temperature = config.temperature,
                        toolRegistry = toolRegistry,
                        maxIterations = config.maxIterations
                    ) {
                        handleEvents {
                            onToolCall { eventContext ->
                                taskToolCalls++
                                synchronized(allEvents) {
                                    allEvents.add(
                                        EventData(
                                            LocalDateTime.now(),
                                            EventType.TOOL_CALL,
                                            mapOf(
                                                "taskId" to taskId,
                                                "taskIndex" to index,
                                                "tool" to eventContext.tool.name,
                                                "args" to eventContext.toolArgs.toString()
                                            ),
                                            sessionId
                                        )
                                    )
                                }
                            }
                        }
                    }

                    val result = agent.run(task)
                    val taskDuration = System.currentTimeMillis() - taskStartTime

                    TaskEventResult(
                        taskId = taskId,
                        task = task,
                        result = result,
                        duration = taskDuration,
                        toolCallCount = taskToolCalls,
                        success = true
                    )
                }
            }.awaitAll()

            val totalDuration = System.currentTimeMillis() - startTime

            ParallelEventResponse(
                sessionId = sessionId,
                totalTasks = tasks.size,
                completedTasks = results.count { it.success },
                results = results,
                events = allEvents,
                totalDuration = totalDuration,
                averageTaskDuration = results.map { it.duration }.average().toLong()
            )
        } catch (e: Exception) {
            logger.error(e) { "Error in parallel event processing" }
            throw e
        }
    }
}

data class DetailedEventResponse(
    val sessionId: String,
    val message: String,
    val events: List<KoogEventHandlerAgent.EventData>,
    val metrics: Map<String, Any>,
    val success: Boolean
)

data class ParallelEventResponse(
    val sessionId: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val results: List<TaskEventResult>,
    val events: List<KoogEventHandlerAgent.EventData>,
    val totalDuration: Long,
    val averageTaskDuration: Long
)

data class TaskEventResult(
    val taskId: String,
    val task: String,
    val result: String,
    val duration: Long,
    val toolCallCount: Int,
    val success: Boolean
)
