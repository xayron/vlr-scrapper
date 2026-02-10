package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

@Serializable
data class FormRating(
    val date: String,
    val opponent: String,
    val event: String,
    val result: String,
    val currentRating: Int,
    val opponentRating: Int,
    val coreId: String
)
