package com.vlr.scrapper.repository

import com.vlr.scrapper.model.vlr.Event
import com.vlr.scrapper.model.vlr.EventDetail
import com.vlr.scrapper.model.vlr.LiveMatch
import com.vlr.scrapper.model.vlr.Match
import com.vlr.scrapper.model.vlr.MatchDetail
import com.vlr.scrapper.model.vlr.NewsDetail
import com.vlr.scrapper.model.vlr.NewsItem
import com.vlr.scrapper.model.vlr.Player
import com.vlr.scrapper.model.vlr.PlayerSearchResult
import com.vlr.scrapper.model.vlr.PlayerStat
import com.vlr.scrapper.model.vlr.RegionRankings
import com.vlr.scrapper.model.vlr.StreamLink
import com.vlr.scrapper.model.vlr.Team
import com.vlr.scrapper.model.vlr.TeamRanking
import com.vlr.scrapper.model.vlr.TeamSearchResult
import com.vlr.scrapper.model.vlr.Transfer

interface IRemoteRepository {
    fun getUpcomingMatches(page: String): List<Match>
    fun getNewsDetail(urlStr: String): NewsDetail
    fun getCompletedMatches(page: Int = 1): List<Match>
    fun getEvents(): List<Event>
    fun getStats(): List<PlayerStat>
    fun getNews(page: Int = 1): List<NewsItem>
    fun getTransfers(page: Int = 1): List<Transfer>
    fun getTeam(teamId: String): Team
    fun getPlayer(playerId: String): Player
    fun getLiveMatches(): List<LiveMatch>
    fun getEventDetail(eventId: String): EventDetail
    fun getEventDetailByUrl(eventUrl: String): EventDetail
    fun searchTeams(query: String): List<TeamSearchResult>
    fun searchPlayers(query: String): List<PlayerSearchResult>
    fun getMatchesByRegion(region: String, page: Int = 1): List<Match>
    fun getMatchesByEvent(eventId: String, page: Int = 1): List<Match>
    fun getMatchStreams(matchUrl: String): List<StreamLink>
    fun getGlobalRankings(): List<RegionRankings>
    fun getRankingsByRegion(region: String): List<TeamRanking>
    fun getMatchDetails(matchUrl: String): MatchDetail
}