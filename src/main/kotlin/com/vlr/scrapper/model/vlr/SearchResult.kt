package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a team search result
 *
 * @property id Team ID
 * @property name Team name
 * @property tag Team tag/abbreviation
 * @property region Team region
 * @property logo Team logo URL
 * @property url Team page URL
 */
@Serializable
data class TeamSearchResult(
    val id: String,
    val name: String,
    val tag: String,
    val region: String,
    val logo: String?,
    val url: String
)

/**
 * Represents a player search result
 *
 * @property id Player ID
 * @property name Player name (in-game name)
 * @property realName Player's real name
 * @property team Current team
 * @property country Player's country
 * @property url Player page URL
 */
@Serializable
data class PlayerSearchResult(
    val id: String,
    val name: String,
    val realName: String?,
    val team: String?,
    val country: String?,
    val url: String
)
