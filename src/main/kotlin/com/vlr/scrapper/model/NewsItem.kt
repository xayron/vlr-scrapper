package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents a news article
 *
 * @property title Article title
 * @property description Article description
 * @property author Article author
 * @property date Publication date
 * @property url Article URL
 */
@Serializable
data class NewsItem(
    val title: String,
    val description: String,
    val author: String,
    val date: String,
    val url: String
)
