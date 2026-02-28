package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

@Serializable
data class RegionRankings(
    val region: String,
    val rankings: List<TeamRanking>
)
