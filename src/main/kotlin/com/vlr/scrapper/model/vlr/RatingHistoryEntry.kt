package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a single rating history entry for a team
 *
 * @property rank Team's rank at this point in time
 * @property region Region for this ranking
 * @property url URL to the rankings page
 */
@Serializable
data class RatingHistoryEntry(
    val rank: String,
    val region: String,
    val url: String
)
