package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a team's ranking position
 *
 * @property rank Team's rank position
 * @property teamName Team name
 * @property region Team region
 * @property points Ranking points
 * @property url Team page URL
 */
@Serializable
data class TeamRanking(
    val rank: String,
    val teamName: String,
    val region: String,
    val points: String,
    val url: String,
    val logoUrl: String? = null
)
