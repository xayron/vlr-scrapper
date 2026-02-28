package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a single result/placement within an event
 *
 * @property stage Stage name (e.g., "Swiss Stage", "Playoffs")
 * @property rank Placement/rank (e.g., "1st", "4th–5th")
 * @property prize Prize money earned (optional, e.g., "$17,000")
 */
@Serializable
data class PlacementResult(
    val stage: String,
    val rank: String,
    val prize: String?
)

/**
 * Represents a team's placement in an event/tournament
 *
 * @property eventName Name of the event/tournament
 * @property year Year of the event
 * @property results List of placements/results for different stages
 * @property url URL to the event page
 */
@Serializable
data class EventPlacement(
    val eventName: String,
    val year: String,
    val results: List<PlacementResult>,
    val url: String
)
