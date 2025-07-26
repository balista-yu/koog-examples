package com.koog.examples.phase2.dto.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherApiResponse(
    @JsonProperty("name") val cityName: String,
    @JsonProperty("main") val main: Main,
    @JsonProperty("weather") val weather: List<Weather>,
    @JsonProperty("wind") val wind: Wind?,
    @JsonProperty("clouds") val clouds: Clouds?,
    @JsonProperty("sys") val sys: Sys
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Main(
        @JsonProperty("temp") val temp: Double,
        @JsonProperty("feels_like") val feelsLike: Double,
        @JsonProperty("temp_min") val tempMin: Double,
        @JsonProperty("temp_max") val tempMax: Double,
        @JsonProperty("pressure") val pressure: Int,
        @JsonProperty("humidity") val humidity: Int
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Weather(
        @JsonProperty("id") val id: Int,
        @JsonProperty("main") val main: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("icon") val icon: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Wind(
        @JsonProperty("speed") val speed: Double,
        @JsonProperty("deg") val deg: Int?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Clouds(
        @JsonProperty("all") val all: Int
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Sys(
        @JsonProperty("country") val country: String
    )
}
