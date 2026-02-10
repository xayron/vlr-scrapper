package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

@Serializable
data class RankingHistoryEntry(
    val date: String,
    val event: String,
    val rank: String,
    val coreId: String
)
