package com.koog.examples.phase2.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service
class HttpClientService(
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    suspend fun <T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        responseType: Class<T>
    ): T = withContext(Dispatchers.IO) {
        try {
            logger.debug("Sending GET request to: $url")

            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()

            headers.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }

            val request = requestBuilder.build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                throw HttpException(
                    "HTTP request failed with status ${response.statusCode()}: ${response.body()}",
                    response.statusCode()
                )
            }

            logger.debug("Received response: ${response.statusCode()}")
            objectMapper.readValue(response.body(), responseType)
        } catch (e: HttpException) {
            logger.error("HTTP request failed: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during HTTP request", e)
            throw HttpException("Failed to complete HTTP request: ${e.message}", 0, e)
        }
    }

    class HttpException(
        message: String,
        val statusCode: Int,
        cause: Throwable? = null
    ) : RuntimeException(message, cause)
}
