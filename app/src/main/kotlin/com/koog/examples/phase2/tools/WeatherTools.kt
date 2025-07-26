package com.koog.examples.phase2.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.koog.examples.phase2.config.ApiConfig
import com.koog.examples.phase2.dto.api.WeatherApiResponse
import com.koog.examples.phase2.service.HttpClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
@LLMDescription("天気情報を取得するツール群")
class WeatherTools(
    private val apiConfig: ApiConfig,
    private val httpClient: HttpClientService
) : ToolSet {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Tool
    @LLMDescription("指定された都市の現在の天気情報を取得します")
    suspend fun getWeather(
        @LLMDescription("天気を取得したい都市名（日本語または英語）") city: String
    ): String {
        return try {
            logger.info("Fetching weather for city: $city")
            val weather = fetchWeather(city)
            formatWeatherResponse(weather)
        } catch (e: Exception) {
            logger.error("Failed to fetch weather", e)
            "天気情報の取得に失敗しました: ${e.message}"
        }
    }

    @Tool(customName = "getWeatherWithAdvice")
    @LLMDescription("指定された都市の天気情報を取得し、アドバイスも提供します")
    suspend fun getWeatherWithAdvice(
        @LLMDescription("天気を取得したい都市名") city: String
    ): String {
        return try {
            val weather = fetchWeather(city)
            val basicInfo = formatWeatherResponse(weather)
            val advice = generateWeatherAdvice(weather)
            "$basicInfo\n\n$advice"
        } catch (e: Exception) {
            logger.error("Failed to fetch weather with advice", e)
            "天気情報の取得に失敗しました: ${e.message}"
        }
    }

    private suspend fun fetchWeather(city: String): WeatherApiResponse {
        val url = "${apiConfig.openweatherBaseUrl}/weather?" +
                "q=$city" +
                "&appid=${apiConfig.openweatherApiKey}" +
                "&units=metric" +
                "&lang=ja"

        return httpClient.get(url, emptyMap(), WeatherApiResponse::class.java)
    }

    private fun formatWeatherResponse(weather: WeatherApiResponse): String {
        val tempCelsius = weather.main.temp.roundToInt()
        val feelsLike = weather.main.feelsLike.roundToInt()
        val weatherDesc = weather.weather.firstOrNull()?.description ?: "不明"
        val humidity = weather.main.humidity
        val windSpeed = weather.wind?.speed ?: 0.0

        return buildString {
            appendLine("【${weather.cityName}の現在の天気】")
            appendLine("天気: $weatherDesc")
            appendLine("気温: ${tempCelsius}°C（体感温度: ${feelsLike}°C）")
            appendLine("湿度: $humidity%")
            appendLine("風速: ${windSpeed}m/s")

            if (weather.clouds != null) {
                appendLine("雲量: ${weather.clouds.all}%")
            }
        }
    }

    private fun generateWeatherAdvice(weather: WeatherApiResponse): String {
        val temp = weather.main.temp.roundToInt()
        val description = weather.weather.firstOrNull()?.description ?: ""
        val humidity = weather.main.humidity

        val advice = mutableListOf<String>()

        when {
            temp > 30 -> advice.add("非常に暑いです。熱中症に注意し、こまめに水分補給を心がけてください。")
            temp > 25 -> advice.add("暖かい日です。快適に過ごせそうです。")
            temp > 15 -> advice.add("過ごしやすい気温です。")
            temp > 5 -> advice.add("少し肌寒いです。上着があると良いでしょう。")
            else -> advice.add("かなり寒いです。暖かい服装でお出かけください。")
        }

        when {
            description.contains("雨") -> advice.add("傘を忘れずに持って行きましょう。")
            description.contains("雪") -> advice.add("足元に注意して、滑りやすい場所を避けてください。")
            description.contains("晴") && temp > 25 -> advice.add("日差しが強そうです。日焼け対策をおすすめします。")
        }

        if (humidity > 80) {
            advice.add("湿度が高めです。不快指数が高いかもしれません。")
        } else if (humidity < 40) {
            advice.add("空気が乾燥しています。保湿を心がけましょう。")
        }

        return if (advice.isNotEmpty()) {
            "💡 アドバイス:\n${advice.joinToString("\n")}"
        } else {
            ""
        }
    }
}
