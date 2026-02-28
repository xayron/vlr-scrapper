package com.vlr.scrapper.services

import com.vlr.scrapper.model.vlr.*
import com.vlr.scrapper.repository.CacheRepository
import com.vlr.scrapper.repository.IRemoteRepository
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.SetArgs
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class VlrDataService(
    private val repository: IRemoteRepository,
    private val cacheRepository: CacheRepository
) {
    private val logger = LoggerFactory.getLogger(VlrDataService::class.java)

    private fun getCacheExpiryTime(hours: Int): SetArgs {
        return SetArgs.Builder.ex(hours * 3600L)
    }

    suspend fun getUpcomingMatches(page: String): List<Match> {
        val cacheKey = "vlr:upcoming_matches:$page"
        return cacheRepository.get(cacheKey)?.let { data ->
            logger.debug("Cache hit for upcoming matches")
            Json.decodeFromString<List<Match>>(data)
        } ?: repository.getUpcomingMatches(page).also { data ->
            logger.debug("Cache miss for upcoming matches")
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getNewsDetail(urlStr: String): NewsDetail {
        val cacheKey = "vlr:news_detail:$urlStr"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<NewsDetail>(data)
        } ?: repository.getNewsDetail(urlStr).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getCompletedMatches(page: Int = 1): List<Match> {
        val cacheKey = "vlr:completed_matches:$page"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<Match>>(data)
        } ?: repository.getCompletedMatches(page).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getEvents(): List<Event> {
        val cacheKey = "vlr:events"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<Event>>(data)
        } ?: repository.getEvents().also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data), getCacheExpiryTime(6))
        }
    }

    suspend fun getStats(): List<PlayerStat> {
        val cacheKey = "vlr:stats"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<PlayerStat>>(data)
        } ?: repository.getStats().also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data), getCacheExpiryTime(6))
        }
    }

    suspend fun getNews(page: Int = 1): List<NewsItem> {
        val cacheKey = "vlr:news:$page"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<NewsItem>>(data)
        } ?: repository.getNews(page).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getTransfers(page: Int = 1): List<Transfer> {
        val cacheKey = "vlr:transfers:$page"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<Transfer>>(data)
        } ?: repository.getTransfers(page).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getTeam(teamId: String): Team {
        val cacheKey = "vlr:team:$teamId"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<Team>(data)
        } ?: repository.getTeam(teamId).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getPlayer(playerId: String): Player {
        val cacheKey = "vlr:player:$playerId"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<Player>(data)
        } ?: repository.getPlayer(playerId).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getLiveMatches(): List<LiveMatch> {
        val cacheKey = "vlr:live_matches"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<LiveMatch>>(data)
        } ?: repository.getLiveMatches().also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data), getCacheExpiryTime(1))
        }
    }

    suspend fun getEventDetail(eventId: String): EventDetail {
        val cacheKey = "vlr:event_detail:$eventId"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<EventDetail>(data)
        } ?: repository.getEventDetail(eventId).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getEventDetailByUrl(eventUrl: String): EventDetail {
        val cacheKey = "vlr:event_detail_url:$eventUrl"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<EventDetail>(data)
        } ?: repository.getEventDetailByUrl(eventUrl).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data)
            )
        }
    }

    suspend fun searchTeams(query: String): List<TeamSearchResult> {
        val cacheKey = "vlr:search_teams:$query"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<TeamSearchResult>>(data)
        } ?: repository.searchTeams(query).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun searchPlayers(query: String): List<PlayerSearchResult> {
        val cacheKey = "vlr:search_players:$query"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<PlayerSearchResult>>(data)
        } ?: repository.searchPlayers(query).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getMatchesByRegion(region: String, page: Int = 1): List<Match> {
        val cacheKey = "vlr:matches_by_region:$region:$page"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<Match>>(data)
        } ?: repository.getMatchesByRegion(region, page).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getMatchesByEvent(eventId: String, page: Int = 1): List<Match> {
        val cacheKey = "vlr:matches_by_event:$eventId:$page"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<Match>>(data)
        } ?: repository.getMatchesByEvent(eventId, page).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getMatchStreams(matchUrl: String): List<StreamLink> {
        val cacheKey = "vlr:match_streams:$matchUrl"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<StreamLink>>(data)
        } ?: repository.getMatchStreams(matchUrl).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getGlobalRankings(): List<RegionRankings> {
        val cacheKey = "vlr:global_rankings"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<RegionRankings>>(data)
        } ?: repository.getGlobalRankings().also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getRankingsByRegion(region: String): List<TeamRanking> {
        val cacheKey = "vlr:rankings_by_region:$region"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<List<TeamRanking>>(data)
        } ?: repository.getRankingsByRegion(region).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }

    suspend fun getMatchDetails(matchUrl: String): MatchDetail {
        val cacheKey = "vlr:match_details:$matchUrl"
        return cacheRepository.get(cacheKey)?.let { data ->
            Json.decodeFromString<MatchDetail>(data)
        } ?: repository.getMatchDetails(matchUrl).also { data ->
            cacheRepository.set(cacheKey, Json.encodeToString(data))
        }
    }
}