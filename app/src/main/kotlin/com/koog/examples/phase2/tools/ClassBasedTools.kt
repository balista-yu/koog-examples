package com.koog.examples.phase2.tools

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.agents.core.tools.ToolResult
import kotlinx.serialization.Serializable
import java.util.Base64

object Base64EncoderTool : Tool<Base64EncoderTool.Args, Base64EncoderTool.Result>() {

    @Serializable
    data class Args(
        val text: String,
        val operation: String = "encode",
        val urlSafe: Boolean = false
    ) : ToolArgs

    @Serializable
    data class Result(
        val originalText: String,
        val processedText: String,
        val operation: String,
        val urlSafe: Boolean,
        val originalLength: Int,
        val processedLength: Int
    ) : ToolResult {
        override fun toStringDefault(): String {
            return """
                【Base64処理結果】
                操作: $operation
                URLセーフ: ${if (urlSafe) "はい" else "いいえ"}

                元のテキスト（${originalLength}文字）:
                $originalText

                処理後のテキスト（${processedLength}文字）:
                $processedText
            """.trimIndent()
        }
    }

    override val argsSerializer = kotlinx.serialization.serializer<Args>()

    override val descriptor = ToolDescriptor(
        name = "base64_encoder",
        description = "テキストをBase64形式でエンコード/デコードします。URLセーフなエンコーディングもサポートしています。",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "text",
                description = "エンコード/デコードするテキスト",
                type = ToolParameterType.String
            )
        ),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "operation",
                description = "実行する操作（encode または decode）",
                type = ToolParameterType.String
            ),
            ToolParameterDescriptor(
                name = "urlSafe",
                description = "URLセーフなBase64を使用するか（trueまたはfalse）",
                type = ToolParameterType.Boolean
            )
        )
    )

    override suspend fun execute(args: Args): Result {
        val encoder = if (args.urlSafe) {
            Base64.getUrlEncoder()
        } else {
            Base64.getEncoder()
        }

        val decoder = if (args.urlSafe) {
            Base64.getUrlDecoder()
        } else {
            Base64.getDecoder()
        }

        return try {
            val processedText = when (args.operation.lowercase()) {
                "encode" -> {
                    encoder.encodeToString(args.text.toByteArray())
                }
                "decode" -> {
                    String(decoder.decode(args.text))
                }
                else -> throw IllegalArgumentException("操作は 'encode' または 'decode' である必要があります")
            }

            Result(
                originalText = args.text,
                processedText = processedText,
                operation = args.operation,
                urlSafe = args.urlSafe,
                originalLength = args.text.length,
                processedLength = processedText.length
            )
        } catch (e: IllegalArgumentException) {
            throw Exception("Base64処理エラー: ${e.message}")
        }
    }

    override fun encodeResultToString(result: Result): String {
        return result.toStringDefault()
    }
}
