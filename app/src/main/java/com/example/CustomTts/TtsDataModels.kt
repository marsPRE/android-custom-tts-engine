package com.example.CustomTts

import kotlinx.serialization.Serializable

@Serializable
data class TtsRequestPayload(
    val model: String,
    val input: String,
    val voice: String,
    val response_format: String = "mp3",
    val speed: Float = 1.0f
)