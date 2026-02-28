package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents detailed information about an event/tournament
 *
 * @property id Event ID
 * @property name Event name
 * @property dates Event dates
 * @property prizePool Prize pool information
 * @property teams List of participating teams
 * @property brackets Bracket data grouped by paths
 * @property location Event location
 * @property format Tournament format
 * @property logoUrl Event logo/image URL
 * @property url Event page URL
 */
@Serializable
data class EventDetail(
    val id: String,
    val name: String,
    val dates: String,
    val prizePool: String?,
    val teams: List<EventTeam>,
    val brackets: List<EventBracketGroup> = emptyList(),
    val location: String?,
    val format: String?,
    val logoUrl: String? = null,
    val url: String
)

/**
 * Represents a team participating in an event
 *
 * @property name Team name
 * @property tag Team tag/abbreviation
 * @property standing Team's standing/placement (e.g., "#1", "#2", etc.)
 * @property logoUrl Team logo/image URL
 * @property url Team page URL
 */
@Serializable
data class EventTeam(
    val name: String,
    val tag: String?,
    val standing: String?,
    val logoUrl: String?,
    val url: String
)

/**
 * Represents one bracket group/path (e.g., Upper, Middle, Lower)
 */
@Serializable
data class EventBracketGroup(
    val name: String,
    val rounds: List<EventBracketRound>
)

/**
 * Represents a round within a bracket group (e.g., Upper Round 1)
 */
@Serializable
data class EventBracketRound(
    val name: String,
    val matches: List<EventBracketMatch>
)

/**
 * Represents one match in a bracket round
 */
@Serializable
data class EventBracketMatch(
    val team1: EventBracketTeam,
    val team2: EventBracketTeam,
    val status: String?,
    val url: String?
)

/**
 * Represents a team slot in a bracket match
 */
@Serializable
data class EventBracketTeam(
    val name: String,
    val score: String?,
    val isWinner: Boolean,
    val logoUrl: String?
)
