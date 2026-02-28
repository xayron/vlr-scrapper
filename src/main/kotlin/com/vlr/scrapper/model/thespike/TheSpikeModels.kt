package com.vlr.scrapper.model.thespike

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TheSpikeRankings(
    val date: String? = null, val teams: List<TheSpikeRankedTeam> = emptyList()
)

@Serializable
data class TheSpikeRankedTeam(
    val id: Int,
    val title: String,
    val slug: String,
    @SerialName("darkLogoUrl") val darkLogoUrl: String? = null,
    @SerialName("lightLogoUrl") val lightLogoUrl: String? = null,
    val achievements: Int? = null,
    val form: Int? = null,
    @SerialName("totalPoints") val totalPoints: Int? = null,
    @SerialName("rankDifference") val rankDifference: Int? = null,
    @SerialName("rankDirection") val rankDirection: String? = null,
    val players: List<TheSpikePlayerBasic> = emptyList()
)

@Serializable
data class TheSpikePlayerBasic(
    val id: Int? = null,
    val nickname: String,
    val slug: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val country: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("isCaptain") val isCaptain: Boolean? = null
)

@Serializable
data class TheSpikeEvent(
    val id: Int,
    val title: String,
    val slug: String,
    val description: String? = null,
    @SerialName("startDate") val startDate: String? = null,
    @SerialName("endDate") val endDate: String? = null,
    @SerialName("prizePool") val prizePool: Int? = null,
    val currency: String? = null,
    val country: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    val status: String? = null,
    @SerialName("teamCount") val teamCount: Int? = null,
    val type: Int? = null,
    @SerialName("isLive") val isLive: Int? = null,
    @SerialName("isFeatured") val isFeatured: Int? = null,
    @SerialName("hasStream") val hasStream: Int? = null,
    val series: TheSpikeEventSeries? = null,
    @SerialName("mapPool") val mapPool: List<String> = emptyList(),
    val prizes: List<TheSpikePrize> = emptyList(),
    val brackets: List<TheSpikeBracket> = emptyList(),
    val teams: List<TheSpikeEventTeam> = emptyList()
)

@Serializable
data class TheSpikeEventSeries(
    val id: Int? = null, val title: String? = null, val slug: String? = null
)

@Serializable
data class TheSpikePrize(
    val place: String? = null, val prize: String? = null, val teams: List<TheSpikePrizeTeam> = emptyList()
)

@Serializable
data class TheSpikePrizeTeam(
    val id: Int? = null, val title: String? = null, val slug: String? = null
)

@Serializable
data class TheSpikeBracket(
    val id: Int? = null, val title: String? = null, val rounds: List<TheSpikeBracketRound> = emptyList()
)

@Serializable
data class TheSpikeBracketRound(
    val id: Int? = null, val title: String? = null, val matches: List<TheSpikeBracketMatch> = emptyList()
)

@Serializable
data class TheSpikeBracketMatch(
    val id: Int? = null, val teams: List<TheSpikeBracketMatchTeam> = emptyList()
)

@Serializable
data class TheSpikeBracketMatchTeam(
    val id: Int? = null,
    val title: String? = null,
    val score: Int? = null,
    @SerialName("isWinner") val isWinner: Boolean? = null
)

@Serializable
data class TheSpikeEventTeam(
    val id: Int? = null,
    val title: String? = null,
    val slug: String? = null,
    @SerialName("logoUrl") val logoUrl: String? = null,
    @SerialName("darkLogoUrl") val darkLogoUrl: String? = null,
    @SerialName("lightLogoUrl") val lightLogoUrl: String? = null,
    val players: List<TheSpikePlayerBasic> = emptyList()
)

@Serializable
data class TheSpikeMatch(
    val id: Int?,
    @SerialName("matchName") val matchName: String? = null,
    @SerialName("bestOf") val bestOf: Int? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("isFinished") val isFinished: Int? = null,
    @SerialName("isLive") val isLive: Int? = null,
    val event: TheSpikeMatchEvent? = null,
    val teams: List<TheSpikeMatchTeam> = emptyList(),
    val maps: List<TheSpikeMap> = emptyList(),
    val streams: List<TheSpikeStream> = emptyList(),
    val votes: List<TheSpikeVote>? = null,
    val odds: List<TheSpikeOdd>? = null,
    @SerialName("pastMatches") val pastMatches: TheSpikePastMatches? = null,
    @SerialName("headToHead") val headToHead: TheSpikeHeadToHead? = null
)

@Serializable
data class TheSpikeMatchEvent(
    val id: Int? = null,
    val title: String? = null,
    val slug: String? = null,
    @SerialName("logoUrl") val logoUrl: String? = null
)

@Serializable
data class TheSpikeMatchTeam(
    val id: Int,
    val title: String,
    val slug: String? = null,
    val score: Int? = null,
    val players: List<TheSpikeMatchPlayer> = emptyList(),
    @SerialName("bestPlayer") val bestPlayer: TheSpikeMatchPlayer? = null,
    @SerialName("darkLogoUrl") val darkLogoUrl: String? = null,
    @SerialName("lightLogoUrl") val lightLogoUrl: String? = null
)

@Serializable
data class TheSpikeMatchPlayer(
    val id: Int? = null,
    val nickname: String? = null,
    val slug: String? = null,
    val country: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("agentThumbnail") val agentThumbnail: String? = null,
    val stats: TheSpikePlayerStats? = null
)

@Serializable
data class TheSpikePlayerStats(
    val rating: Double? = null,
    val acs: Double? = null,
    val kills: Int? = null,
    val deaths: Int? = null,
    val assists: Int? = null,
    @SerialName("firstKills") val firstKills: Int? = null,
    @SerialName("firstDeaths") val firstDeaths: Int? = null
)

@Serializable
data class TheSpikeMap(
    val id: Int? = null,
    val map: TheSpikeMapInfo? = null,
    @SerialName("pickTeamId") val pickTeamId: Int? = null,
    @SerialName("isDecider") val isDecider: Boolean? = null,
    val teams: List<TheSpikeMapTeam> = emptyList()
)

@Serializable
data class TheSpikeMapInfo(
    val id: Int? = null,
    val title: String? = null,
    val slug: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null
)

@Serializable
data class TheSpikeMapTeam(
    val id: Int? = null,
    val score: Int? = null,
    @SerialName("isWinner") val isWinner: Boolean? = null,
    val players: List<TheSpikeMapPlayer> = emptyList()
)

@Serializable
data class TheSpikeMapPlayer(
    val id: Int? = null,
    val nickname: String? = null,
    val agent: TheSpikeAgent? = null,
    val stats: TheSpikePlayerStats? = null
)

@Serializable
data class TheSpikeAgent(
    val id: Int? = null,
    val title: String? = null,
    val slug: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null
)

@Serializable
data class TheSpikeStream(
    val id: Int? = null,
    val title: String? = null,
    val url: String? = null,
    val platform: String? = null,
    val language: String? = null
)

@Serializable
data class TheSpikeVote(
    val id: Int? = null, @SerialName("teamId") val teamId: Int? = null, val votes: Int? = null
)

@Serializable
data class TheSpikeOdd(
    val id: Int? = null,
    @SerialName("teamId") val teamId: Int? = null,
    val odds: Double? = null,
    val bookmaker: String? = null
)

@Serializable
data class TheSpikePastMatches(
    val team1: List<TheSpikePastMatch> = emptyList(), val team2: List<TheSpikePastMatch> = emptyList()
)

@Serializable
data class TheSpikePastMatch(
    val id: Int? = null,
    @SerialName("startTime") val startTime: String? = null,
    val opponent: TheSpikeMatchTeam? = null,
    val score: Int? = null,
    @SerialName("opponentScore") val opponentScore: Int? = null,
    @SerialName("isWinner") val isWinner: Boolean? = null
)

@Serializable
data class TheSpikeHeadToHead(
    @SerialName("team1Wins") val team1Wins: Int? = null,
    @SerialName("team2Wins") val team2Wins: Int? = null,
    val matches: List<TheSpikeMatch> = emptyList()
)

@Serializable
data class TheSpikeTeam(
    val id: Int,
    val title: String,
    val slug: String,
    val description: String? = null,
    @SerialName("darkLogoUrl") val darkLogoUrl: String? = null,
    @SerialName("lightLogoUrl") val lightLogoUrl: String? = null,
    @SerialName("facebookUrl") val facebookUrl: String? = null,
    @SerialName("twitterUrl") val twitterUrl: String? = null,
    @SerialName("instagramUrl") val instagramUrl: String? = null,
    @SerialName("twitchUrl") val twitchUrl: String? = null,
    @SerialName("youtubeUrl") val youtubeUrl: String? = null,
    @SerialName("websiteUrl") val websiteUrl: String? = null,
    val country: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("rankingNo") val rankingNo: Int? = null,
    @SerialName("totalWinnings") val totalWinnings: Long? = null,
    @SerialName("teamAchievements") val teamAchievements: List<TheSpikeAchievement> = emptyList(),
    @SerialName("pastPlayers") val pastPlayers: List<TheSpikePastPlayer> = emptyList(),
    val players: List<TheSpikeTeamPlayer> = emptyList(),
    val matches: List<TheSpikeMatch> = emptyList()
)

@Serializable
data class TheSpikeAchievement(
    val id: Int? = null,
    val event: TheSpikeEvent? = null,
    val placement: String? = null,
    val prize: String? = null,
    val date: String? = null
)

@Serializable
data class TheSpikePastPlayer(
    val id: Int? = null,
    val nickname: String? = null,
    val slug: String? = null,
    val joined: String? = null,
    val left: String? = null
)

@Serializable
data class TheSpikeTeamPlayer(
    val id: Int,
    val nickname: String,
    val slug: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val country: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("isCaptain") val isCaptain: Boolean? = null,
    val role: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    val agents: List<TheSpikeAgent> = emptyList(),
    val stats: TheSpikePlayerSeasonStats? = null
)

@Serializable
data class TheSpikePlayerSeasonStats(
    val rating: Double? = null,
    val acs: Double? = null,
    @SerialName("kdRatio") val kdRatio: Double? = null,
    @SerialName("kast") val kast: Double? = null,
    val adr: Double? = null,
    @SerialName("kpr") val kpr: Double? = null,
    @SerialName("apr") val apr: Double? = null,
    @SerialName("fkpr") val fkpr: Double? = null,
    @SerialName("fdpr") val fdpr: Double? = null,
    @SerialName("hsPercentage") val hsPercentage: Double? = null,
    val clutches: Int? = null,
    @SerialName("clutchRounds") val clutchRounds: Int? = null,
    @SerialName("clutchPercentage") val clutchPercentage: Double? = null,
    val rounds: Int? = null,
    val maps: Int? = null
)

@Serializable
data class TheSpikeNewsResponse(
    val news: TheSpikeNewsData? = null, val categories: List<TheSpikeNewsCategory> = emptyList()
)

@Serializable
data class TheSpikeNewsData(
    @SerialName("pageCount") val pageCount: Int? = null,
    val news: List<TheSpikeNewsItem> = emptyList(),
    @SerialName("newsCount") val newsCount: Int? = null
)

@Serializable
data class TheSpikeNewsItem(
    val id: Int,
    val title: String,
    @SerialName("metaTitle") val metaTitle: String? = null,
    @SerialName("metaDescription") val metaDescription: String? = null,
    @SerialName("bannerImage") val bannerImage: String? = null,
    @SerialName("thumbnailImage") val thumbnailImage: String? = null,
    @SerialName("publishedOn") val publishedOn: String? = null,
    val country: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    val slug: String? = null,
    val category: TheSpikeNewsCategory? = null,
    val content: String? = null,
    val author: TheSpikeNewsAuthor? = null
)

@Serializable
data class TheSpikeNewsCategory(
    val id: Int? = null, val title: String? = null, val slug: String? = null
)

@Serializable
data class TheSpikeNewsAuthor(
    val id: Int? = null, val name: String? = null, val avatar: String? = null
)

@Serializable
data class TheSpikeEventsResponse(
    val ongoing: List<TheSpikeEvent> = emptyList(), val upcoming: List<TheSpikeEvent> = emptyList()
)

@Serializable
data class TheSpikeMatchesResponse(
    val live: List<TheSpikeMatch> = emptyList(),
    val upcoming: List<TheSpikeMatch> = emptyList(),
    val completed: List<TheSpikeMatch> = emptyList()
)
