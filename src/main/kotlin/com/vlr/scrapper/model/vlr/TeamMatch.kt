package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a team's match (recent or upcoming)
 *
 * @property eventName Name of the tournament/event
 * @property eventStage Stage of the event (e.g., "LR2", "Finals")
 * @property opponent Name of the opposing team
 * @property score Match score (e.g., "2:1"), null for upcoming matches
 * @property date Date and time of the match
 * @property url URL to the match details page
 * @property isUpcoming Whether this is an upcoming match (true) or recent result (false)
 */
@Serializable
data class TeamMatch(
    val eventName: String,
    val eventStage: String?,
    val opponent: String,
    val score: String?,
    val date: String,
    val url: String,
    val isUpcoming: Boolean
)
