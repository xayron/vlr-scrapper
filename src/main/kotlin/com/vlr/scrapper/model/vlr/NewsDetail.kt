package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents detailed information about a news article
 *
 * @property title Article title
 * @property author Article author
 * @property date Publication date
 * @property content Main text content of the article
 * @property url URL of the article
 * @property comments List of comments/thread
 */
@Serializable
data class NewsDetail(
    val title: String,
    val author: String,
    val date: String,
    val content: String,
    val url: String,
    val comments: List<ThreadComment>
)
