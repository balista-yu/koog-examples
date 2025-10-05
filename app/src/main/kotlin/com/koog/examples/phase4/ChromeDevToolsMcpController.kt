package com.koog.examples.phase4

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Chrome DevTools MCP REST APIコントローラー
 *
 * Chrome DevTools MCPサービスへのHTTPエンドポイントを提供します。
 */
@RestController
@RequestMapping("/phase4/chrome-devtools")
class ChromeDevToolsMcpController(
    private val chromeDevToolsService: ChromeDevToolsMcpService
) {

    /**
     * サービスの状態を確認
     */
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(chromeDevToolsService.getStatus())
    }

    /**
     * 利用可能なツールの一覧を取得
     */
    @GetMapping("/tools")
    fun getTools(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "tools" to chromeDevToolsService.getAvailableTools(),
                "count" to chromeDevToolsService.getAvailableTools().size
            )
        )
    }

    /**
     * AIエージェントを使用してタスクを実行
     *
     * Example:
     * ```
     * curl -X POST http://localhost:8080/phase4/chrome-devtools/execute \
     *   -H "Content-Type: application/json" \
     *   -d '{"task": "https://example.comにアクセスしてスクリーンショットを撮って"}'
     * ```
     */
    @PostMapping("/execute")
    fun executeTask(@RequestBody request: TaskRequest): ResponseEntity<Map<String, Any>> {
        if (!chromeDevToolsService.isReady()) {
            return ResponseEntity.status(503).body(
                mapOf("error" to "Chrome DevTools MCP Service is not ready")
            )
        }

        val result = chromeDevToolsService.executeTask(request.task)
        return ResponseEntity.ok(
            mapOf(
                "task" to request.task,
                "result" to result
            )
        )
    }

    /**
     * サンプルタスクの一覧を取得
     */
    @GetMapping("/examples")
    fun getExamples(): ResponseEntity<Map<String, Any>> {
        val examples = listOf(
            mapOf(
                "name" to "スクリーンショット撮影",
                "task" to "https://example.comにアクセスしてスクリーンショットを撮ってください"
            ),
            mapOf(
                "name" to "パフォーマンス分析",
                "task" to "https://example.comのページ読み込みパフォーマンスを分析してください"
            ),
            mapOf(
                "name" to "フォーム入力",
                "task" to "https://example.comにアクセスして、検索フォームに「test」と入力してください"
            ),
            mapOf(
                "name" to "ネットワークリクエスト監視",
                "task" to "https://example.comにアクセスして、すべてのネットワークリクエストをリストアップしてください"
            ),
            mapOf(
                "name" to "コンソールログ確認",
                "task" to "https://example.comにアクセスして、コンソールにエラーがないか確認してください"
            ),
            mapOf(
                "name" to "JavaScriptの実行",
                "task" to "https://example.comにアクセスして、document.titleを取得してください"
            ),
            mapOf(
                "name" to "レスポンシブ検証",
                "task" to "https://example.comをモバイルサイズ(375x667)でスクリーンショットを撮ってください"
            )
        )

        return ResponseEntity.ok(
            mapOf(
                "examples" to examples,
                "usage" to "POST /phase4/chrome-devtools/execute with {\"task\": \"<example_task>\"}"
            )
        )
    }

    data class TaskRequest(val task: String)
}
