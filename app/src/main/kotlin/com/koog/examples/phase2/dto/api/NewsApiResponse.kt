package com.koog.examples.phase2.dto.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiResponse(
    @JsonProperty("status") val status: String,
    @JsonProperty("totalResults") val totalResults: Int?,
    @JsonProperty("articles") val articles: List<Article>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Article(
        @JsonProperty("source") val source: Source?,
        @JsonProperty("author") val author: String?,
        @JsonProperty("title") val title: String,
        @JsonProperty("description") val description: String?,
        @JsonProperty("url") val url: String,
        @JsonProperty("urlToImage") val urlToImage: String?,
        @JsonProperty("publishedAt") val publishedAt: String,
        @JsonProperty("content") val content: String?
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Source(
            @JsonProperty("id") val id: String?,
            @JsonProperty("name") val name: String
        )
    }
}
