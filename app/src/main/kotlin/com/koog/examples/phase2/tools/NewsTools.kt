package com.koog.examples.phase2.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.koog.examples.phase2.config.ApiConfig
import com.koog.examples.phase2.dto.api.NewsApiResponse
import com.koog.examples.phase2.service.HttpClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
@LLMDescription("ニュース情報を検索・取得するツール群")
class NewsTools(
    private val apiConfig: ApiConfig,
    private val httpClient: HttpClientService
) : ToolSet {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Tool
    @LLMDescription("キーワードに基づいてニュースを検索します")
    suspend fun searchNews(
        @LLMDescription("検索キーワード") query: String,
        @LLMDescription("取得する記事数（1-20、デフォルト: 5）") limit: Int = 5
    ): String {
        return try {
            logger.info("Searching news for query: $query, limit: $limit")
            val news = searchNewsApi(query, limit)
            formatNewsResponse(news, limit)
        } catch (e: Exception) {
            logger.error("Failed to search news", e)
            "ニュース検索に失敗しました: ${e.message}"
        }
    }

    @Tool
    @LLMDescription("最新のヘッドラインニュースを取得します")
    suspend fun getTopHeadlines(
        @LLMDescription("国コード（jp=日本、us=アメリカなど）") country: String = "jp",
        @LLMDescription("カテゴリー（business, entertainment, general, health, science, sports, technology）") category: String? = null,
        @LLMDescription("取得する記事数（1-20）") limit: Int = 5
    ): String {
        return try {
            logger.info("Fetching top headlines - country: $country, category: $category, limit: $limit")
            val news = fetchTopHeadlines(country, category, limit)
            formatNewsResponse(news, limit)
        } catch (e: Exception) {
            logger.error("Failed to fetch top headlines", e)
            "ヘッドライン取得に失敗しました: ${e.message}"
        }
    }

    private suspend fun searchNewsApi(query: String, limit: Int): NewsApiResponse {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)
        val url = "${apiConfig.newsApiBaseUrl}/everything?" +
                "q=$encodedQuery" +
                "&language=ja" +
                "&sortBy=publishedAt" +
                "&pageSize=$limit" +
                "&apiKey=${apiConfig.newsApiKey}"

        return httpClient.get(url, emptyMap(), NewsApiResponse::class.java)
    }

    private suspend fun fetchTopHeadlines(
        country: String,
        category: String?,
        limit: Int
    ): NewsApiResponse {
        val url = buildString {
            append("${apiConfig.newsApiBaseUrl}/top-headlines?")
            append("country=$country")
            category?.let { append("&category=$it") }
            append("&pageSize=$limit")
            append("&apiKey=${apiConfig.newsApiKey}")
        }

        return httpClient.get(url, emptyMap(), NewsApiResponse::class.java)
    }

    private fun formatNewsResponse(news: NewsApiResponse, limit: Int): String {
        if (news.articles.isEmpty()) {
            return "該当するニュースが見つかりませんでした。"
        }

        return buildString {
            appendLine("【最新ニュース】")
            appendLine("取得件数: ${news.articles.take(limit).size}件")

            news.articles.take(limit).forEachIndexed { index, article ->
                appendLine("\n${index + 1}. ${article.title}")
                article.source?.name?.let { appendLine("   出典: $it") }

                val publishedAt = try {
                    val dateTime = LocalDateTime.parse(article.publishedAt, DateTimeFormatter.ISO_DATE_TIME)
                    dateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))
                } catch (e: Exception) {
                    article.publishedAt
                }
                appendLine("   公開日: $publishedAt")

                article.description?.let { desc ->
                    val truncatedDesc = if (desc.length > 100) {
                        desc.substring(0, 100) + "..."
                    } else {
                        desc
                    }
                    appendLine("   概要: $truncatedDesc")
                }

                appendLine("   URL: ${article.url}")
            }

            news.totalResults?.let {
                if (it > limit) {
                    appendLine("\n※ 他にも${it - limit}件の記事があります")
                }
            }
        }
    }
}
