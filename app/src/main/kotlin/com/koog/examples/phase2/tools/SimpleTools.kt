package com.koog.examples.phase2.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * UUIDを生成するシンプルなツール（アノテーションベース）
 */
@Component
@LLMDescription("UUID生成ツール")
class UUIDGeneratorToolSet : ToolSet {

    @Tool
    @LLMDescription("UUID（Universally Unique Identifier）を生成します")
    fun generateUUID(
        @LLMDescription("生成するUUIDの個数（1-10、デフォルト: 1）") count: Int = 1,
        @LLMDescription("UUIDのフォーマット（standard, compact, uppercase）") format: String = "standard"
    ): String {
        return try {
            val actualCount = count.coerceIn(1, 10)

            val uuids = List(actualCount) {
                val uuid = UUID.randomUUID().toString()
                formatUUID(uuid, format)
            }

            buildString {
                appendLine("【UUID生成結果】")
                appendLine("生成数: $actualCount")
                appendLine("フォーマット: $format")
                appendLine()
                uuids.forEachIndexed { index, uuid ->
                    appendLine("${index + 1}. $uuid")
                }
            }.trim()
        } catch (e: Exception) {
            "UUID生成エラー: ${e.message}"
        }
    }

    private fun formatUUID(uuid: String, format: String): String {
        return when (format.lowercase()) {
            "compact" -> uuid.replace("-", "")
            "uppercase" -> uuid.uppercase()
            else -> uuid
        }
    }
}
