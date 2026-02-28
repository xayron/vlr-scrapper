package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a live/ongoing match
 *
 * @property team1 First team name
 * @property team2 Second team name
 * @property score1 First team's current score
 * @property score2 Second team's current score
 * @property event Event name
 * @property status Match status (e.g., "LIVE", "Map 1", etc.)
 * @property team1CountryFlag First team's country flag URL
 * @property team2CountryFlag Second team's country flag URL
 * @property eventIcon Event icon URL
 * @property url Match page URL
 */
@Serializable
data class LiveMatch(
    val team1: String,
    val team2: String,
    val score1: String,
    val score2: String,
    val event: String,
    val status: String,
    val team1CountryFlag: String?,
    val team2CountryFlag: String?,
    val eventIcon: String?,
    val url: String
)
