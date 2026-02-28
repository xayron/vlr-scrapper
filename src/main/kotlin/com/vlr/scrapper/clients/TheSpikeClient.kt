package com.vlr.scrapper.clients

import com.vlr.scrapper.model.thespike.TheSpikeEvent
import com.vlr.scrapper.model.thespike.TheSpikeEventsResponse
import com.vlr.scrapper.model.thespike.TheSpikeMatch
import com.vlr.scrapper.model.thespike.TheSpikeMatchesResponse
import com.vlr.scrapper.model.thespike.TheSpikeNewsResponse
import com.vlr.scrapper.model.thespike.TheSpikeRankings
import com.vlr.scrapper.model.thespike.TheSpikeTeam
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class TheSpikeClient {
    private val baseUrl = "https://api.thespike.gg"
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    suspend fun getRankings(region: Int? = null): TheSpikeRankings {
        val url = if (region != null) {
            "$baseUrl/rankings?region=$region"
        } else {
            "$baseUrl/rankings"
        }
        val response = client.get(url)
        return response.body()
    }
    
    suspend fun getEvents(region: Int? = null): TheSpikeEventsResponse {
        val url = if (region != null) {
            "$baseUrl/events?region=$region"
        } else {
            "$baseUrl/events"
        }
        return client.get(url).body()
    }
    
    suspend fun getEvent(eventId: Int): TheSpikeEvent {
        return client.get("$baseUrl/events/$eventId").body()
    }
    
    suspend fun getMatches(region: Int? = null): TheSpikeMatchesResponse {
        val url = if (region != null) {
            "$baseUrl/matches?region=$region"
        } else {
            "$baseUrl/matches"
        }
        val response: List<TheSpikeMatch> = client.get(url).body()
        return TheSpikeMatchesResponse(
            live = response.filter { it.isLive == 1 },
            upcoming = response.filter { it.isLive != 1 && it.isFinished != 1 },
            completed = response.filter { it.isFinished == 1 }
        )
    }
    
    suspend fun getLiveMatches(region: Int? = null): List<TheSpikeMatch> {
        val url = if (region != null) {
            "$baseUrl/matches?region=$region"
        } else {
            "$baseUrl/matches"
        }
        val response: List<TheSpikeMatch> = client.get(url).body()
        return response.filter { it.isLive == 1 }
    }
    
    suspend fun getMatch(matchId: Int): TheSpikeMatch {
        return client.get("$baseUrl/match/$matchId").body()
    }
    
    suspend fun getTeam(teamId: Int): TheSpikeTeam {
        return client.get("$baseUrl/teams/$teamId").body()
    }
    
    suspend fun getNews(
        page: Int? = null,
        category: String? = null,
        country: String? = null
    ): TheSpikeNewsResponse {
        val params = buildList {
            if (page != null) add("page=$page")
            if (category != null) add("category=$category")
            if (country != null) add("country=$country")
        }
        val url = if (params.isNotEmpty()) {
            "$baseUrl/news?${params.joinToString("&")}"
        } else {
            "$baseUrl/news"
        }
        return client.get(url).body()
    }
    
    fun getRegionName(regionId: Int): String {
        return when (regionId) {
            1 -> "Americas"
            2 -> "EMEA"
            3 -> "Pacific"
            4 -> "Japan"
            5 -> "Brazil"
            6 -> "LATAM North"
            7 -> "Southeast Asia"
            9 -> "China/MENA"
            10 -> "LATAM South"
            else -> "Unknown"
        }
    }
    
    fun getAllRegions(): Map<Int, String> {
        return mapOf(
            1 to "Americas",
            2 to "EMEA",
            3 to "Pacific",
            4 to "Japan",
            5 to "Brazil",
            6 to "LATAM North",
            7 to "Southeast Asia",
            9 to "China/MENA",
            10 to "LATAM South"
        )
    }
}
