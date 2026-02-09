package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents a Valorant team
 *
 * @property id Team ID
 * @property name Team name
 * @property tag Team tag/abbreviation
 * @property logo Team logo URL
 * @property region Team region
 * @property roster List of roster members
 * @property url Team page URL
 */
@Serializable
data class Team(
    val id: String,
    val name: String,
    val tag: String,
    val logo: String?,
    val region: String,
    val roster: List<RosterMember>,
    val url: String
)

/**
 * Represents a member of a team's roster
 *
 * @property name Player/staff name (in-game name)
 * @property realName Real name
 * @property role Role in the team (player, coach, manager, etc.)
 * @property url Player/staff page URL
 */
@Serializable
data class RosterMember(
    val name: String,
    val realName: String?,
    val role: String?,
    val url: String
)
