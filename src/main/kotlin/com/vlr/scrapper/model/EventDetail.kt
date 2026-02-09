package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents detailed information about an event/tournament
 *
 * @property id Event ID
 * @property name Event name
 * @property dates Event dates
 * @property prizePool Prize pool information
 * @property teams List of participating teams
 * @property location Event location
 * @property format Tournament format
 * @property url Event page URL
 */
@Serializable
data class EventDetail(
    val id: String,
    val name: String,
    val dates: String,
    val prizePool: String?,
    val teams: List<EventTeam>,
    val location: String?,
    val format: String?,
    val url: String
)

/**
 * Represents a team participating in an event
 *
 * @property name Team name
 * @property tag Team tag/abbreviation
 * @property standing Team's standing/placement (e.g., "#1", "#2", etc.)
 * @property url Team page URL
 */
@Serializable
data class EventTeam(
    val name: String,
    val tag: String?,
    val standing: String?,
    val url: String
)
