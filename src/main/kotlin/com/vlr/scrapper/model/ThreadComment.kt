package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents a comment in a news thread
 *
 * @property author Name of the comment author
 * @property content Text content of the comment
 * @property url URL to the specific comment (if available)
 */
@Serializable
data class ThreadComment(
    val author: String,
    val content: String,
    val url: String? = null,
    val children: List<ThreadComment> = emptyList()
)
