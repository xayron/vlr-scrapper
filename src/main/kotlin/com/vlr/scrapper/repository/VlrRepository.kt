package com.vlr.scrapper.repository

import com.vlr.scrapper.clients.VlrScraperClient
import com.vlr.scrapper.model.vlr.*

class VlrRepository(
    private val client: VlrScraperClient = VlrScraperClient()
) : IRemoteRepository {

    override fun getUpcomingMatches(page: String): List<Match> {
        return client.getUpcomingMatches(page)
    }

    override fun getNewsDetail(urlStr: String): NewsDetail {
        return client.getNewsDetail(urlStr)
    }

    override fun getCompletedMatches(page: Int): List<Match> {
        return client.getCompletedMatches(page)
    }

    override fun getEvents(): List<Event> {
        return client.getEvents()
    }

    override fun getStats(): List<PlayerStat> {
        return client.getStats()
    }

    override fun getNews(page: Int): List<NewsItem> {
        return client.getNews(page)
    }

    override fun getTransfers(page: Int): List<Transfer> {
        return client.getTransfers(page)
    }

    override fun getTeam(teamId: String): Team {
        return client.getTeam(teamId)
    }

    override fun getPlayer(playerId: String): Player {
        return client.getPlayer(playerId)
    }

    override fun getLiveMatches(): List<LiveMatch> {
        return client.getLiveMatches()
    }

    override fun getEventDetail(eventId: String): EventDetail {
        return client.getEventDetail(eventId)
    }

    override fun getEventDetailByUrl(eventUrl: String): EventDetail {
        return client.getEventDetailByUrl(eventUrl)
    }

    override fun searchTeams(query: String): List<TeamSearchResult> {
        return client.searchTeams(query)
    }

    override fun searchPlayers(query: String): List<PlayerSearchResult> {
        return client.searchPlayers(query)
    }

    override fun getMatchesByRegion(region: String, page: Int): List<Match> {
        return client.getMatchesByRegion(region, page)
    }

    override fun getMatchesByEvent(eventId: String, page: Int): List<Match> {
        return client.getMatchesByEvent(eventId, page)
    }

    override fun getMatchStreams(matchUrl: String): List<StreamLink> {
        return client.getMatchStreams(matchUrl)
    }

    override fun getGlobalRankings(): List<RegionRankings> {
        return client.getGlobalRankings()
    }

    override fun getRankingsByRegion(region: String): List<TeamRanking> {
        return client.getRankingsByRegion(region)
    }

    override fun getMatchDetails(matchUrl: String): MatchDetail {
        return client.getMatchDetails(matchUrl)
    }
}
