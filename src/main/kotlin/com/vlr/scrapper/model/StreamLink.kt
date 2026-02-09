package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents a streaming link for a match
 *
 * @property platform Streaming platform (e.g., "Twitch", "YouTube", etc.)
 * @property url Stream URL
 * @property language Stream language
 */
@Serializable
data class StreamLink(
    val platform: String,
    val url: String,
    val language: String?
)
