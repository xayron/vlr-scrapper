package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a Valorant team with comprehensive information
 *
 * @property id Team ID
 * @property name Team name
 * @property tag Team tag/abbreviation
 * @property logoUrl URL to the team's logo image
 * @property region Team's region/country
 * @property socialLinks Map of social media platform names to URLs
 * @property totalWinnings Total prize money earned by the team
 * @property roster List of current players
 * @property recentMatches List of recent match results
 * @property upcomingMatches List of upcoming scheduled matches
 * @property eventPlacements List of tournament placements/results
 * @property ratingHistory List of historical ranking entries
 * @property url Team page URL
 */
@Serializable
data class Team(
    val id: String,
    val name: String,
    val tag: String?,
    val logoUrl: String?,
    val region: String?,
    val socialLinks: Map<String, String>,
    val totalWinnings: String?,
    val roster: List<TeamPlayer>,
    val recentMatches: List<TeamMatch>,
    val upcomingMatches: List<TeamMatch>,
    val eventPlacements: List<EventPlacement>,
    val ratingHistory: List<RatingHistoryEntry>,
    val formRating: List<FormRating> = emptyList(),
    val rankingHistory: List<RankingHistoryEntry> = emptyList(),
    val url: String
)
