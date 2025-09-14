package com.koog.examples.phase4

import ai.koog.agents.agent.AIAgent
import ai.koog.agents.llm.openai.OpenAIModels
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.prompts.Prompt
import ai.koog.agents.prompts.SimplePromptExecutor
import ai.koog.agents.strategy.agent.graph.StringSubgraphResult
import ai.koog.agents.strategy.agent.graph.agentStrategies
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Phase 4: MCPリソース管理の実装
 *
 * MCPリソースの取得、キャッシング、更新通知を管理する実装例
 */
@Component
class McpResourceManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // リソースキャッシュ
    private val resourceCache = ConcurrentHashMap<String, CachedResource>()

    // リソース更新のFlow
    private val _resourceUpdates = MutableSharedFlow<ResourceUpdate>()
    val resourceUpdates: SharedFlow<ResourceUpdate> = _resourceUpdates.asSharedFlow()

    /**
     * MCPリソースの取得と管理
     */
    suspend fun manageResources() {
        logger.info { "Starting MCP resource management..." }

        // MCPサーバーへの接続
        val transport = McpToolRegistryProvider.defaultSseTransport(
            url = "http://localhost:8931/sse"
        )

        // リソース監視ジョブの開始
        val monitoringJob = launch {
            monitorResourceChanges()
        }

        // リソースの初期ロード
        loadInitialResources()

        // リソース更新の処理
        resourceUpdates.collect { update ->
            handleResourceUpdate(update)
        }

        monitoringJob.cancel()
    }

    /**
     * 初期リソースのロード
     */
    private suspend fun loadInitialResources() {
        logger.info { "Loading initial resources..." }

        // サンプルリソースの作成
        val resources = listOf(
            McpResource(
                id = "config-1",
                type = ResourceType.CONFIG,
                name = "Application Configuration",
                content = """
                    {
                        "apiEndpoint": "https://api.example.com",
                        "timeout": 30000,
                        "retryCount": 3
                    }
                """.trimIndent(),
                metadata = mapOf(
                    "version" to "1.0.0",
                    "lastModified" to Instant.now().toString()
                )
            ),
            McpResource(
                id = "template-1",
                type = ResourceType.PROMPT_TEMPLATE,
                name = "Data Analysis Prompt",
                content = """
                    Analyze the following data and provide insights:
                    {data}

                    Focus on:
                    1. Trends and patterns
                    2. Anomalies
                    3. Recommendations
                """.trimIndent(),
                metadata = mapOf(
                    "category" to "analysis",
                    "language" to "en"
                )
            ),
            McpResource(
                id = "knowledge-1",
                type = ResourceType.KNOWLEDGE_BASE,
                name = "Domain Knowledge",
                content = """
                    # Domain Knowledge Base

                    ## Key Concepts
                    - Machine Learning: Statistical methods for pattern recognition
                    - Natural Language Processing: Understanding human language
                    - Computer Vision: Image and video analysis

                    ## Best Practices
                    - Always validate input data
                    - Use appropriate evaluation metrics
                    - Consider ethical implications
                """.trimIndent(),
                metadata = mapOf(
                    "domain" to "AI/ML",
                    "lastUpdated" to Instant.now().toString()
                )
            )
        )

        // リソースをキャッシュに追加
        resources.forEach { resource ->
            cacheResource(resource)
            logger.info { "Loaded resource: ${resource.id} (${resource.type})" }
        }
    }

    /**
     * リソースのキャッシング
     */
    private fun cacheResource(resource: McpResource) {
        resourceCache[resource.id] = CachedResource(
            resource = resource,
            cachedAt = Instant.now(),
            accessCount = 0
        )
    }

    /**
     * リソースの取得（キャッシュ対応）
     */
    fun getResource(resourceId: String): McpResource? {
        val cached = resourceCache[resourceId]
        if (cached != null) {
            // アクセスカウントを増やす
            cached.accessCount++
            logger.debug { "Resource cache hit: $resourceId (access count: ${cached.accessCount})" }

            // キャッシュの有効期限チェック（例: 1時間）
            val cacheAge = Instant.now().epochSecond - cached.cachedAt.epochSecond
            if (cacheAge > 3600) {
                logger.info { "Resource cache expired: $resourceId" }
                // 実際の実装では、ここで新しいリソースを取得
                refreshResource(resourceId)
            }

            return cached.resource
        }

        logger.debug { "Resource cache miss: $resourceId" }
        return null
    }

    /**
     * リソースのリフレッシュ
     */
    private fun refreshResource(resourceId: String) {
        coroutineScope.launch {
            logger.info { "Refreshing resource: $resourceId" }
            // 実際のMCPサーバーからリソースを再取得
            // ここではダミーの更新を行う
            val existing = resourceCache[resourceId]
            if (existing != null) {
                val updated = existing.resource.copy(
                    metadata = existing.resource.metadata + mapOf(
                        "refreshedAt" to Instant.now().toString()
                    )
                )
                cacheResource(updated)
                _resourceUpdates.emit(
                    ResourceUpdate(
                        resourceId = resourceId,
                        updateType = UpdateType.REFRESHED,
                        resource = updated
                    )
                )
            }
        }
    }

    /**
     * リソース変更の監視
     */
    private suspend fun monitorResourceChanges() {
        logger.info { "Starting resource change monitoring..." }

        // 定期的にリソースの更新をチェック（実際はMCPサーバーからの通知を受信）
        while (currentCoroutineContext().isActive) {
            delay(30000) // 30秒ごとにチェック

            // キャッシュ統計の出力
            logCacheStatistics()

            // 古いキャッシュのクリーンアップ
            cleanupOldCache()
        }
    }

    /**
     * リソース更新の処理
     */
    private suspend fun handleResourceUpdate(update: ResourceUpdate) {
        logger.info { "Handling resource update: ${update.resourceId} (${update.updateType})" }

        when (update.updateType) {
            UpdateType.CREATED -> {
                update.resource?.let { cacheResource(it) }
            }
            UpdateType.UPDATED -> {
                update.resource?.let { cacheResource(it) }
            }
            UpdateType.DELETED -> {
                resourceCache.remove(update.resourceId)
            }
            UpdateType.REFRESHED -> {
                // Already handled in refreshResource
            }
        }
    }

    /**
     * キャッシュ統計のログ出力
     */
    private fun logCacheStatistics() {
        val totalResources = resourceCache.size
        val totalAccesses = resourceCache.values.sumOf { it.accessCount }
        val avgAccessCount = if (totalResources > 0) totalAccesses / totalResources else 0

        logger.info {
            """
            Cache Statistics:
            - Total Resources: $totalResources
            - Total Accesses: $totalAccesses
            - Average Access Count: $avgAccessCount
            - Most Accessed: ${getMostAccessedResource()}
            """.trimIndent()
        }
    }

    /**
     * 最もアクセスされたリソースの取得
     */
    private fun getMostAccessedResource(): String {
        return resourceCache.entries
            .maxByOrNull { it.value.accessCount }
            ?.let { "${it.key} (${it.value.accessCount} accesses)" }
            ?: "N/A"
    }

    /**
     * 古いキャッシュのクリーンアップ
     */
    private fun cleanupOldCache() {
        val now = Instant.now()
        val maxAge = 7200L // 2時間

        val toRemove = resourceCache.entries.filter { entry ->
            val age = now.epochSecond - entry.value.cachedAt.epochSecond
            age > maxAge && entry.value.accessCount == 0
        }

        toRemove.forEach { entry ->
            logger.info { "Removing old cached resource: ${entry.key}" }
            resourceCache.remove(entry.key)
        }
    }

    /**
     * リソースベースのエージェント実行
     */
    suspend fun runResourceAwareAgent() {
        logger.info { "Running resource-aware agent..." }

        // 初期リソースのロード
        loadInitialResources()

        val executor = SimplePromptExecutor(
            apiKey = System.getenv("OPENAI_API_KEY") ?: throw IllegalStateException("OPENAI_API_KEY not set")
        )

        // プロンプトテンプレートリソースの取得
        val promptTemplate = getResource("template-1")
        val knowledgeBase = getResource("knowledge-1")

        val strategy = agentStrategies {
            graph<StringSubgraphResult> {
                val result = agent {
                    name = "resource-aware-agent"
                    description = "Agent that uses MCP resources"
                    prompt = buildString {
                        appendLine("You are an AI assistant with access to resources.")
                        knowledgeBase?.let {
                            appendLine("\nKnowledge Base:")
                            appendLine(it.content)
                        }
                        promptTemplate?.let {
                            appendLine("\nUse this template for analysis:")
                            appendLine(it.content)
                        }
                    }
                    outputSchema = StringSubgraphResult::class
                }
                result
            }
        }

        val agent = AIAgent(
            executor = executor,
            strategy = strategy,
            llmModel = OpenAIModels.Chat.GPT4o,
            toolRegistry = null
        )

        val result = agent.execute(
            Prompt("Analyze the importance of resource management in AI systems")
        )

        logger.info { "Agent result: $result" }
    }

    companion object {
        private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
}

/**
 * MCPリソース
 */
@Serializable
data class McpResource(
    val id: String,
    val type: ResourceType,
    val name: String,
    val content: String,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * リソースタイプ
 */
enum class ResourceType {
    CONFIG,
    PROMPT_TEMPLATE,
    KNOWLEDGE_BASE,
    DATA,
    MODEL
}

/**
 * キャッシュされたリソース
 */
data class CachedResource(
    val resource: McpResource,
    val cachedAt: Instant,
    var accessCount: Int
)

/**
 * リソース更新イベント
 */
data class ResourceUpdate(
    val resourceId: String,
    val updateType: UpdateType,
    val resource: McpResource? = null
)

/**
 * 更新タイプ
 */
enum class UpdateType {
    CREATED,
    UPDATED,
    DELETED,
    REFRESHED
}

/**
 * メイン実行関数
 */
fun main() = runBlocking {
    val manager = McpResourceManager()

    // リソース管理の実行
    launch {
        manager.manageResources()
    }

    // リソース対応エージェントの実行
    manager.runResourceAwareAgent()

    delay(5000) // 5秒待機
}