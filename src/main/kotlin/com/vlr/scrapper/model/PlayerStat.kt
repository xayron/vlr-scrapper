package com.vlr.scrapper.model

import kotlinx.serialization.Serializable

/**
 * Represents comprehensive player statistics from VLR.gg
 *
 * @property player Player name
 * @property team Team name
 * @property agents List of agents played
 * @property mapsPlayed Number of maps played
 * @property rating Player rating
 * @property acs Average Combat Score
 * @property kd Kill/Death Ratio
 * @property kast Kill, Assist, Survive, Trade Percentage
 * @property adr Average Damage per Round
 * @property kpr Kills per Round
 * @property apr Assists per Round
 * @property fkpr First Kills per Round
 * @property fdpr First Deaths per Round
 * @property hsPercentage Headshot Percentage
 * @property clPercentage Clutch Percentage
 * @property clutches Clutches won/played
 * @property maxKills Maximum kills in a single map
 * @property kills Total Kills
 * @property deaths Total Deaths
 * @property assists Total Assists
 * @property firstKills Total First Kills
 * @property firstDeaths Total First Deaths
 */
@Serializable
data class PlayerStat(
    val player: String,
    val team: String,
    val agents: List<String>,
    val mapsPlayed: String,
    val rating: String,
    val acs: String,
    val kd: String,
    val kast: String,
    val adr: String,
    val kpr: String,
    val apr: String,
    val fkpr: String,
    val fdpr: String,
    val hsPercentage: String,
    val clPercentage: String,
    val clutches: String,
    val maxKills: String,
    val kills: String,
    val deaths: String,
    val assists: String,
    val firstKills: String,
    val firstDeaths: String
)
