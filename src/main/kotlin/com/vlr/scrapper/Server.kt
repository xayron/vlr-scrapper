package com.vlr.scrapper

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import io.ktor.server.plugins.cors.routing.*

/**
 * Main entry point for the VLR Scraper API server
 *
 * Starts an embedded Netty server on port 8080
 */
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Configures the Ktor application module
 *
 * Sets up:
 * - JSON content negotiation with pretty printing
 * - VLR Scraper instance
 * - API routing endpoints
 */
fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowHeader(HttpHeaders.Authorization)
        allowHost("*")
    }

    val scraper = VlrScraper()

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        get("/") {
            call.respondText("VLR Scraper API is running! Check /swagger for documentation.")
        }

        // ============ MATCH ENDPOINTS ============

        route("/matches") {
            get("/upcoming") {
                try {
                    val page : String = call.request.queryParameters["page"] ?: "1"
                    println("Page: $page")
                    val matches = scraper.getUpcomingMatches(page)
                    call.respond(matches)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
                }
            }

            get("/completed") {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val matches = scraper.getCompletedMatches(page)
                    call.respond(matches)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
                }
            }

            get("/details") {
                val url = call.request.queryParameters["url"]
                if (url == null) {
                    call.respondText("Missing 'url' query parameter", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val details = scraper.getMatchDetails(url)
                    call.respond(details)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
                }
            }
        }

        // ============ NEWS ENDPOINT ============

        get("/news") {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val news = scraper.getNews(page)
                call.respond(news)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }

        // ============ TRANSFERS ENDPOINT ============

        get("/transfers") {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val transfers = scraper.getTransfers(page)
                call.respond(transfers)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }

        // ============ EVENTS ENDPOINT ============

        get("/events") {
            try {
                val events = scraper.getEvents()
                call.respond(events)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }

        // ============ RANKINGS ENDPOINT ============

        get("/rankings") {
            try {
                val rankings = scraper.getGlobalRankings()
                call.respond(rankings)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }

        // ============ STATS ENDPOINT ============

        get("/stats") {
            try {
                val stats = scraper.getStats()
                call.respond(stats)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ TEAM ENDPOINT ============
        
        get("/team/{id}") {
            val teamId = call.parameters["id"]
            if (teamId == null) {
                call.respondText("Missing team ID", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val team = scraper.getTeam(teamId)
                call.respond(team)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ PLAYER ENDPOINT ============
        
        get("/player/{id}") {
            val playerId = call.parameters["id"]
            if (playerId == null) {
                call.respondText("Missing player ID", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val player = scraper.getPlayer(playerId)
                call.respond(player)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ LIVE MATCHES ENDPOINT ============
        
        get("/live") {
            try {
                val liveMatches = scraper.getLiveMatches()
                call.respond(liveMatches)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ EVENT DETAIL ENDPOINT ============
        
        get("/event/{id}") {
            val eventId = call.parameters["id"]
            if (eventId == null) {
                call.respondText("Missing event ID", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val eventDetail = scraper.getEventDetail(eventId)
                call.respond(eventDetail)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ SEARCH ENDPOINTS ============
        
        route("/search") {
            get("/teams") {
                val query = call.request.queryParameters["q"]
                if (query == null) {
                    call.respondText("Missing 'q' query parameter", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val results = scraper.searchTeams(query)
                    call.respond(results)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
                }
            }
            
            get("/players") {
                val query = call.request.queryParameters["q"]
                if (query == null) {
                    call.respondText("Missing 'q' query parameter", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val results = scraper.searchPlayers(query)
                    call.respond(results)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
                }
            }
        }
        
        // ============ FILTER ENDPOINTS ============
        
        get("/matches/region/{region}") {
            val region = call.parameters["region"]
            if (region == null) {
                call.respondText("Missing region parameter", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val matches = scraper.getMatchesByRegion(region, page)
                call.respond(matches)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        get("/matches/event/{eventId}") {
            val eventId = call.parameters["eventId"]
            if (eventId == null) {
                call.respondText("Missing event ID", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val matches = scraper.getMatchesByEvent(eventId, page)
                call.respond(matches)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        get("/rankings/region/{region}") {
            val region = call.parameters["region"]
            if (region == null) {
                call.respondText("Missing region parameter", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val rankings = scraper.getRankingsByRegion(region)
                call.respond(rankings)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ STREAM ENDPOINTS ============
        
        get("/streams") {
            val matchUrl = call.request.queryParameters["url"]
            if (matchUrl == null) {
                call.respondText("Missing 'url' query parameter", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val streams = scraper.getMatchStreams(matchUrl)
                call.respond(streams)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }

        

    }
}
