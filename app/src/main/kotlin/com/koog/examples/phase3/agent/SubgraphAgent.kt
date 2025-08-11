package com.koog.examples.phase3.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.StringSubgraphResult
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
import java.util.UUID

/**
 * Koogのサブグラフ機能を活用したエージェント実装
 * StringSubgraphResultを使用してサブグラフの実行結果を管理
 */
@Component
class SubgraphAgent(
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

    /**
     * サブグラフを使用した処理パイプライン
     */
    suspend fun processWithSubgraph(userMessage: String): SubgraphResponse {
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        try {
            logger.info { "Starting subgraph processing for session: $sessionId" }

            // 分析サブグラフ
            val analysisResult = executeAnalysisSubgraph(userMessage, sessionId)

            // 処理サブグラフ
            val processingResult = executeProcessingSubgraph(
                analysisResult.result,
                sessionId
            )

            // 最終化サブグラフ
            val finalResult = executeFinalizationSubgraph(
                processingResult.result,
                sessionId
            )

            val duration = System.currentTimeMillis() - startTime

            return SubgraphResponse(
                sessionId = sessionId,
                analysisResult = analysisResult,
                processingResult = processingResult,
                finalResult = finalResult,
                duration = duration,
                success = true
            )
        } catch (e: Exception) {
            logger.error(e) { "Error in subgraph processing" }

            return SubgraphResponse(
                sessionId = sessionId,
                analysisResult = StringSubgraphResult("Analysis failed: ${e.message}"),
                processingResult = StringSubgraphResult("Processing failed"),
                finalResult = StringSubgraphResult("Error: ${e.message}"),
                duration = System.currentTimeMillis() - startTime,
                success = false
            )
        }
    }

    /**
     * 並列サブグラフ実行
     */
    suspend fun processParallelSubgraphs(tasks: List<String>): ParallelSubgraphResponse = coroutineScope {
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        logger.info { "Starting parallel subgraph processing for ${tasks.size} tasks" }

        try {
            val results = tasks.mapIndexed { index, task ->
                async {
                    val taskId = UUID.randomUUID().toString()
                    val taskStartTime = System.currentTimeMillis()

                    val result = executeTaskSubgraph(task, taskId, sessionId)

                    SubgraphTaskResult(
                        taskId = taskId,
                        taskIndex = index,
                        task = task,
                        result = result,
                        duration = System.currentTimeMillis() - taskStartTime
                    )
                }
            }.awaitAll()

            // 結果を集約
            val aggregatedResult = aggregateResults(results)

            ParallelSubgraphResponse(
                sessionId = sessionId,
                totalTasks = tasks.size,
                taskResults = results,
                aggregatedResult = aggregatedResult,
                totalDuration = System.currentTimeMillis() - startTime,
                success = true
            )
        } catch (e: Exception) {
            logger.error(e) { "Error in parallel subgraph processing" }
            throw e
        }
    }

    /**
     * 分析サブグラフの実行
     */
    private suspend fun executeAnalysisSubgraph(
        input: String,
        sessionId: String
    ): StringSubgraphResult {
        logger.debug { "Executing analysis subgraph for session: $sessionId" }

        val agent = createSubgraphAgent("分析タスク: 入力を分析し、主要な要素を特定する")

        val analysisPrompt = """
        以下の入力を分析してください：
        $input

        分析項目：
        1. 主要なトピック
        2. 必要なアクション
        3. 期待される出力
        """.trimIndent()

        val result = agent.run(analysisPrompt)

        return StringSubgraphResult(result)
    }

    /**
     * 処理サブグラフの実行
     */
    private suspend fun executeProcessingSubgraph(
        analysisResult: String,
        sessionId: String
    ): StringSubgraphResult {
        logger.debug { "Executing processing subgraph for session: $sessionId" }

        val agent = createSubgraphAgent("処理タスク: 分析結果に基づいて処理を実行")

        val processingPrompt = """
        分析結果に基づいて処理を実行してください：
        $analysisResult

        処理手順：
        1. 必要な情報を収集
        2. 適切な処理を実行
        3. 結果を生成
        """.trimIndent()

        val result = agent.run(processingPrompt)

        return StringSubgraphResult(result)
    }

    /**
     * 最終化サブグラフの実行
     */
    private suspend fun executeFinalizationSubgraph(
        processingResult: String,
        sessionId: String
    ): StringSubgraphResult {
        logger.debug { "Executing finalization subgraph for session: $sessionId" }

        val agent = createSubgraphAgent("最終化タスク: 結果を整形して出力")

        val finalizationPrompt = """
        処理結果を最終的な形式に整形してください：
        $processingResult

        出力要件：
        1. 明確で簡潔な形式
        2. ユーザーにとって理解しやすい内容
        3. 実行可能なアクションがある場合は明記
        """.trimIndent()

        val result = agent.run(finalizationPrompt)

        return StringSubgraphResult(result)
    }

    /**
     * タスクサブグラフの実行
     */
    private suspend fun executeTaskSubgraph(
        task: String,
        taskId: String,
        sessionId: String
    ): StringSubgraphResult {
        logger.debug { "Executing task subgraph: $taskId" }

        val agent = createSubgraphAgent("タスク実行: $task")
        val result = agent.run(task)

        return StringSubgraphResult(result)
    }

    /**
     * 結果の集約
     */
    private fun aggregateResults(results: List<SubgraphTaskResult>): StringSubgraphResult {
        val aggregated = results.joinToString(separator = "\n\n") { taskResult ->
            """
            タスク ${taskResult.taskIndex + 1}:
            入力: ${taskResult.task}
            結果: ${taskResult.result.result}
            実行時間: ${taskResult.duration}ms
            """.trimIndent()
        }

        return StringSubgraphResult(
            """
            === 並列サブグラフ実行結果 ===
            合計タスク数: ${results.size}

            $aggregated

            === サマリー ===
            平均実行時間: ${results.map { it.duration }.average()}ms
            """.trimIndent()
        )
    }

    /**
     * サブグラフ用エージェントの作成
     */
    private fun createSubgraphAgent(systemPrompt: String): AIAgent<String, String> {
        return AIAgent(
            llmModel = GoogleModels.Gemini2_0Flash001,
            executor = executor,
            systemPrompt = systemPrompt,
            temperature = config.temperature,
            toolRegistry = toolRegistry,
            maxIterations = 5
        ) {
            handleEvents {
                onToolCall { eventContext ->
                    logger.trace { "Subgraph tool call: ${eventContext.tool.name}" }
                }
            }
        }
    }
}

data class SubgraphResponse(
    val sessionId: String,
    val analysisResult: StringSubgraphResult,
    val processingResult: StringSubgraphResult,
    val finalResult: StringSubgraphResult,
    val duration: Long,
    val success: Boolean
)

data class ParallelSubgraphResponse(
    val sessionId: String,
    val totalTasks: Int,
    val taskResults: List<SubgraphTaskResult>,
    val aggregatedResult: StringSubgraphResult,
    val totalDuration: Long,
    val success: Boolean
)

data class SubgraphTaskResult(
    val taskId: String,
    val taskIndex: Int,
    val task: String,
    val result: StringSubgraphResult,
    val duration: Long
)
