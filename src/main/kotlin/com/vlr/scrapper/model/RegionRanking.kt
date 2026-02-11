package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

@Serializable
data class RegionRanking(
    val region: String,
    val rankings: List<TeamRanking>
)
