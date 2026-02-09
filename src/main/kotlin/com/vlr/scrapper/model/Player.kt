package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents a Valorant player
 *
 * @property id Player ID
 * @property name Player name (in-game name)
 * @property realName Player's real name
 * @property country Player's country
 * @property countryFlag Country flag URL
 * @property team Current team
 * @property agents List of agents played
 * @property pastTeams List of past teams
 * @property url Player page URL
 */
@Serializable
data class Player(
    val id: String,
    val name: String,
    val realName: String?,
    val country: String?,
    val countryFlag: String?,
    val team: String?,
    val agents: List<String>,
    val pastTeams: List<PastTeam>,
    val url: String
)

/**
 * Represents a player's past team
 *
 * @property name Team name
 * @property period Time period with the team
 * @property url Team page URL
 */
@Serializable
data class PastTeam(
    val name: String,
    val period: String?,
    val url: String
)
