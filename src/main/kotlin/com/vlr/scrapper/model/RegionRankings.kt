package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

@Serializable
data class RegionRankings(
    val region: String,
    val rankings: List<TeamRanking>
)
