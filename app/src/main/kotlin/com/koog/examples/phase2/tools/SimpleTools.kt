package com.koog.examples.phase2.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import java.util.UUID

object UUIDGeneratorTool : SimpleTool<UUIDGeneratorTool.Args>() {
    @Serializable
    data class Args(
        val count: Int = 1,
        val format: String = "standard"
    ) : ToolArgs

    override val argsSerializer = kotlinx.serialization.serializer<Args>()

    override val descriptor = ToolDescriptor(
        name = "uuid_generator",
        description = "UUID（Universally Unique Identifier）を生成します",
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "count",
                description = "生成するUUIDの個数（1-10、デフォルト: 1）",
                type = ToolParameterType.Integer
            ),
            ToolParameterDescriptor(
                name = "format",
                description = "UUIDのフォーマット（standard, compact, uppercase）",
                type = ToolParameterType.String
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        return try {
            val count = args.count.coerceIn(1, 10)

            val uuids = List(count) {
                val uuid = UUID.randomUUID().toString()
                formatUUID(uuid, args.format)
            }

            buildString {
                appendLine("【UUID生成結果】")
                appendLine("生成数: $count")
                appendLine("フォーマット: ${args.format}")
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
