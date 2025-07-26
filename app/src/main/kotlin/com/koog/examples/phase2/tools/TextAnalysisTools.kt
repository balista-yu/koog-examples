package com.koog.examples.phase2.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import org.springframework.stereotype.Component

@Component
@LLMDescription("テキストを分析するツール群")
class TextAnalysisTools : ToolSet {

    @Tool
    @LLMDescription("テキストの基本的な統計情報を分析します")
    fun analyzeText(
        @LLMDescription("分析するテキスト") text: String
    ): String {
        val charCount = text.length
        val charCountNoSpaces = text.replace(Regex("\\s"), "").length
        val lineCount = text.lines().size
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size

        return buildString {
            appendLine("【テキスト分析結果】")
            appendLine("📊 基本統計:")
            appendLine("・文字数: ${charCount}文字（スペース除く: ${charCountNoSpaces}文字）")
            appendLine("・行数: ${lineCount}行")
            appendLine("・単語数: ${wordCount}語")
        }
    }

    @Tool
    @LLMDescription("テキスト内の URL、メールアドレス、ハッシュタグなどを抽出します")
    fun extractPatterns(
        @LLMDescription("パターンを抽出するテキスト") text: String
    ): String {
        val urlPattern = Regex("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+")
        val urls = urlPattern.findAll(text).map { it.value }.toList()

        val emailPattern = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        val emails = emailPattern.findAll(text).map { it.value }.toList()

        val hashtagPattern = Regex("#[\\w\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF]+")
        val hashtags = hashtagPattern.findAll(text).map { it.value }.toList()

        val numberPattern = Regex("\\d+")
        val numbers = numberPattern.findAll(text).map { it.value }.toList()

        return buildString {
            appendLine("【パターン抽出結果】")

            if (urls.isNotEmpty()) {
                appendLine("\n🔗 URL (${urls.size}件):")
                urls.forEach { appendLine("・$it") }
            }

            if (emails.isNotEmpty()) {
                appendLine("\n📧 メールアドレス (${emails.size}件):")
                emails.forEach { appendLine("・$it") }
            }

            if (hashtags.isNotEmpty()) {
                appendLine("\n#️⃣ ハッシュタグ (${hashtags.size}件):")
                hashtags.forEach { appendLine("・$it") }
            }

            if (numbers.isNotEmpty()) {
                appendLine("\n🔢 数値 (${numbers.size}件):")
                appendLine("・${numbers.joinToString(", ")}")
            }

            if (urls.isEmpty() && emails.isEmpty() && hashtags.isEmpty() && numbers.isEmpty()) {
                appendLine("特定のパターンは見つかりませんでした。")
            }
        }
    }

    @Tool
    @LLMDescription("テキストの文字種別（ひらがな、カタカナ、漢字など）を分析します")
    fun analyzeCharacterTypes(
        @LLMDescription("文字種別を分析するテキスト") text: String
    ): String {
        val hiragana = text.count { it in '\u3040'..'\u309F' }
        val katakana = text.count { it in '\u30A0'..'\u30FF' }
        val kanji = text.count { it in '\u4E00'..'\u9FAF' }
        val alphabet = text.count { it in 'a'..'z' || it in 'A'..'Z' }
        val digit = text.count { it.isDigit() }
        val space = text.count { it.isWhitespace() }
        val other = text.length - hiragana - katakana - kanji - alphabet - digit - space

        val japanesePattern = Regex("[\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF]")
        val hasJapanese = japanesePattern.containsMatchIn(text)

        val englishWordPattern = Regex("\\b[a-zA-Z]+\\b")
        val englishWords = englishWordPattern.findAll(text).count()

        val readingTime = (text.length / 400.0).let {
            if (it < 1) "1分未満" else "${it.toInt()}分"
        }

        return buildString {
            appendLine("【文字種別分析】")
            appendLine("\n📈 文字種別内訳:")
            appendLine("・ひらがな: ${hiragana}文字 (${(hiragana * 100.0 / text.length).format(1)}%)")
            appendLine("・カタカナ: ${katakana}文字 (${(katakana * 100.0 / text.length).format(1)}%)")
            appendLine("・漢字: ${kanji}文字 (${(kanji * 100.0 / text.length).format(1)}%)")
            appendLine("・アルファベット: ${alphabet}文字 (${(alphabet * 100.0 / text.length).format(1)}%)")
            appendLine("・数字: ${digit}文字 (${(digit * 100.0 / text.length).format(1)}%)")
            appendLine("・空白: ${space}文字 (${(space * 100.0 / text.length).format(1)}%)")
            if (other > 0) {
                appendLine("・その他: ${other}文字 (${(other * 100.0 / text.length).format(1)}%)")
            }

            appendLine("\n🌐 言語判定:")
            appendLine("・日本語を含む: ${if (hasJapanese) "はい" else "いいえ"}")
            appendLine("・英単語数: ${englishWords}語")

            appendLine("\n⏱️ 推定読了時間: $readingTime（400文字/分として計算）")
        }
    }

    private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
}
