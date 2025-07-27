package com.koog.examples.phase2.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Component
@LLMDescription("テキストを分析するツール群")
class TextAnalysisTools : ToolSet {

    private val logger = LoggerFactory.getLogger(this::class.java)

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

    @Tool
    @LLMDescription("URLからWebページのコンテンツを取得して分析します")
    suspend fun analyzeUrl(
        @LLMDescription("分析するWebページのURL") url: String
    ): String {
        return try {
            logger.info("Fetching content from URL: $url")

            // URLバリデーション
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return "エラー: 有効なURLを入力してください（http://またはhttps://で始まる必要があります）"
            }

            // HTTPリクエストでHTMLコンテンツを取得（直接HTTP通信を行う）
            val htmlContent = fetchHtmlContent(url)

            // HTMLタグを除去してテキストのみを抽出
            val textContent = extractTextFromHtml(htmlContent)

            // 抽出したテキストを分析
            val analysis = buildString {
                appendLine("【URL分析結果】")
                appendLine("🌐 URL: $url")
                appendLine()

                // 基本的な統計情報
                val basicAnalysis = analyzeText(textContent)
                appendLine(basicAnalysis)

                // パターン抽出
                val patterns = extractPatterns(textContent)
                appendLine("\n$patterns")

                // 文字種別分析
                val charTypes = analyzeCharacterTypes(textContent)
                appendLine("\n$charTypes")
            }

            analysis
        } catch (e: Exception) {
            logger.error("Failed to analyze URL: $url", e)
            "URLの分析に失敗しました: ${e.message}"
        }
    }

    private fun extractTextFromHtml(html: String): String {
        // シンプルなHTMLタグ除去
        var text = html
            // スクリプトとスタイルタグの内容を削除
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            // HTMLタグを削除
            .replace(Regex("<[^>]+>"), " ")
            // HTMLエンティティをデコード
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            // 連続する空白を1つに
            .replace(Regex("\\s+"), " ")
            .trim()

        return text
    }

    private suspend fun fetchHtmlContent(url: String): String = withContext(Dispatchers.IO) {
        try {
            val httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()

            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                throw Exception("HTTP request failed with status ${response.statusCode()}")
            }

            response.body()
        } catch (e: Exception) {
            logger.error("Failed to fetch HTML content from URL: $url", e)
            throw e
        }
    }
}
