package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a match (upcoming or completed)
 *
 * @property team1 First team name
 * @property team2 Second team name
 * @property time Match time/date
 * @property event Event name
 * @property score1 First team's score (null if upcoming)
 * @property score2 Second team's score (null if upcoming)
 * @property url Match page URL
 * @property team1CountryFlag First team's country flag URL
 * @property team2CountryFlag Second team's country flag URL
 * @property eventIcon Event icon URL
 * @property streams List of available streams
 */
@Serializable
data class Match(
    val team1: String,
    val team2: String,
    val time: String,
    val event: String,
    val score1: String?,
    val score2: String?,
    val url: String,
    val team1CountryFlag: String? = null,
    val team2CountryFlag: String? = null,
    val eventIcon: String? = null,
    val streams: List<StreamLink>? = null
)
