package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents a Valorant event/tournament
 *
 * @property id Event ID
 * @property name Event name
 * @property status Event status (e.g., "Ongoing", "Upcoming", "Completed")
 * @property dates Event dates
 * @property region Event region
 * @property prizePool Prize pool information
 * @property url Event page URL
 */
@Serializable
data class Event(
    val id: String,
    val name: String,
    val status: String,
    val dates: String,
    val region: String,
    val prizePool: String,
    val image: String?,
    val url: String
)
