package com.koog.examples.phase5.dto

data class OllamaRequest(
    val message: String
)

data class OllamaResponse(
    val message: String,
    val response: String,
    val model: String,
    val success: Boolean
)
