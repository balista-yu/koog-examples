package com.koog.examples.phase3.dto

data class EventDrivenRequest(
    val message: String
)

data class ParallelRequest(
    val tasks: List<String>
)
