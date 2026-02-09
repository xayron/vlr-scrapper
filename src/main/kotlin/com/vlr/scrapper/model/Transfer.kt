package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents a player transfer
 *
 * @property player Player name
 * @property fromTeam Team the player is leaving
 * @property toTeam Team the player is joining
 * @property date Transfer date
 * @property url Player page URL
 */
@Serializable
data class Transfer(
    val player: String,
    val fromTeam: String,
    val toTeam: String,
    val date: String,
    val url: String
)
