package com.vlr.scrapper.model.vlr

import kotlinx.serialization.Serializable

/**
 * Represents a player in a team's roster
 *
 * @property alias Player's in-game alias/nickname
 * @property realName Player's real name (optional)
 * @property url URL to the player's profile page
 * @property imageUrl URL to the player's avatar/photo (optional)
 * @property roles List of role tags (e.g., "SUB", "INACTIVE", "COACH")
 */
@Serializable
data class TeamPlayer(
    val alias: String,
    val realName: String?,
    val url: String,
    val imageUrl: String?,
    val roles: List<String>
)
