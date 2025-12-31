package com.koog.examples.phase2.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import org.springframework.stereotype.Component
import java.util.Base64

/**
 * Base64エンコード/デコードツール（アノテーションベース）
 */
@Component
@LLMDescription("Base64エンコード/デコードツール")
class Base64EncoderToolSet : ToolSet {

    @Tool
    @LLMDescription("テキストをBase64形式でエンコード/デコードします。URLセーフなエンコーディングもサポートしています。")
    fun base64Process(
        @LLMDescription("エンコード/デコードするテキスト") text: String,
        @LLMDescription("実行する操作（encode または decode）") operation: String = "encode",
        @LLMDescription("URLセーフなBase64を使用するか（trueまたはfalse）") urlSafe: Boolean = false
    ): String {
        val encoder = if (urlSafe) {
            Base64.getUrlEncoder()
        } else {
            Base64.getEncoder()
        }

        val decoder = if (urlSafe) {
            Base64.getUrlDecoder()
        } else {
            Base64.getDecoder()
        }

        return try {
            val processedText = when (operation.lowercase()) {
                "encode" -> {
                    encoder.encodeToString(text.toByteArray())
                }
                "decode" -> {
                    String(decoder.decode(text))
                }
                else -> throw IllegalArgumentException("操作は 'encode' または 'decode' である必要があります")
            }

            """
                【Base64処理結果】
                操作: $operation
                URLセーフ: ${if (urlSafe) "はい" else "いいえ"}

                元のテキスト（${text.length}文字）:
                $text

                処理後のテキスト（${processedText.length}文字）:
                $processedText
            """.trimIndent()
        } catch (e: IllegalArgumentException) {
            "Base64処理エラー: ${e.message}"
        }
    }
}
