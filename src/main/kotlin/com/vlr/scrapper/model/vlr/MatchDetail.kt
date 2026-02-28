package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Comprehensive match details from VLR.gg match detail pages
 */
@Serializable
data class MatchDetail(
    val eventName: String,
    val eventImage: String?,
    val matchSubtitle: String,
    val date: String,
    val time: String,
    val patch: String?,
    val team1: TeamInfo,
    val team2: TeamInfo,
    val status: String,
    val timeUntilMatch: String?,  // e.g., "2H 8M" for upcoming matches, null for completed/live
    val format: String,
    val bansPicksInfo: String?,
    val streams: List<StreamInfo>,
    val maps: List<MapResult>,
    val mapStats: MapStats
)

/**
 * Container for all map statistics
 */
@Serializable
data class MapStats(
    val allMaps: MapStatsData?,
    val map1: MapStatsData?,
    val map2: MapStatsData?,
    val map3: MapStatsData?,
    val map4: MapStatsData?,
    val map5: MapStatsData?
)

@Serializable
data class TeamInfo(
    val name: String,
    val logo: String?,
    val link: String?,
    val score: String?
)

@Serializable
data class StreamInfo(
    val name: String,
    val url: String
)

/**
 * Represents a team's score breakdown for a map
 */
@Serializable
data class TeamScore(
    val total: String,
    val attack: String?,
    val defend: String?
)

@Serializable
data class MapResult(
    val mapName: String,
    val team1Score: TeamScore,
    val team2Score: TeamScore,
    val winner: String?,
    val pickedBy: String?,  // "team1", "team2", or null if not specified
    val duration: String?   // e.g., "51:55"
)

/**
 * Stats and round data for a specific map
 */
@Serializable
data class MapStatsData(
    val mapName: String,
    val playerStats: List<PlayerMatchStat>,
    val rounds: List<Round>
)

/**
 * Represents a single round in a map
 */
@Serializable
data class Round(
    val roundNum: String,
    val score: String,           // e.g., "1-0", "7-5"
    val winner: String,          // "team1" or "team2"
    val t: String,               // "team1" or "team2"
    val ct: String,              // "team1" or "team2"
    val winType: String,         // "elimination", "bomb_exploded", "bomb_defused", "time_ran_out"
    val winIconUrl: String?      // URL to the win type icon
)

/**
 * Represents a stat value broken down by side (All/Attack/Defend)
 */
@Serializable
data class SideStat(
    val all: String,
    val attack: String,
    val defend: String
)

@Serializable
data class PlayerMatchStat(
    val playerName: String,
    val team: String,
    val agents: List<String>,
    val rating: SideStat,
    val acs: SideStat,
    val kills: SideStat,
    val deaths: SideStat,
    val assists: SideStat,
    val kdDiff: SideStat,
    val kast: SideStat,
    val adr: SideStat,
    val hs: SideStat,
    val fk: SideStat,
    val fd: SideStat,
    val fkDiff: SideStat
)
