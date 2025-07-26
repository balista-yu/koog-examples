package com.koog.examples.phase2.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "api")
class ApiConfig {
    var openweatherApiKey: String = ""
    var newsApiKey: String = ""
    var openweatherBaseUrl: String = "https://api.openweathermap.org/data/2.5"
    var newsApiBaseUrl: String = "https://newsapi.org/v2"
    var requestTimeout: Long = 30000
    var maxRetries: Int = 3
    
    fun validateApiKeys() {
        if (openweatherApiKey.isBlank()) {
            throw IllegalStateException("OpenWeather API key is not configured. Please set OPENWEATHER_API_KEY environment variable.")
        }
        if (newsApiKey.isBlank()) {
            throw IllegalStateException("News API key is not configured. Please set NEWS_API_KEY environment variable.")
        }
    }
}
