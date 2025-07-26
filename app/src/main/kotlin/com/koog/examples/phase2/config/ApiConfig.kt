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
}
