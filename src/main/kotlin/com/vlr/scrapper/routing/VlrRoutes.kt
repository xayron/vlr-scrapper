package com.vlr.scrapper.routing

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode

import com.vlr.scrapper.services.VlrDataService

fun Route.vlrRoutes(dataService: VlrDataService) {
    route("/") {


        // ============ MATCH ENDPOINTS ============

        route("/matches") {
            get("/live") {
                try {
                    val liveMatches = dataService.getLiveMatches()
                    call.respond(liveMatches)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }

            get("/upcoming") {
                try {
                    val page : String = call.request.queryParameters["page"] ?: "1"
                    val matches = dataService.getUpcomingMatches(page)
                    call.respond(matches)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }

            get("/completed") {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val matches = dataService.getCompletedMatches(page)
                    call.respond(matches)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }

            get("/details") {
                val url = call.request.queryParameters["url"]
                if (url == null) {
                    call.respondText("Missing 'url' query parameter", status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val details = dataService.getMatchDetails(url)
                    call.respond(details)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }

            get("/region/{region}") {
                val region = call.parameters["region"]
                if (region == null) {
                    call.respondText("Missing region parameter", status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val matches = dataService.getMatchesByRegion(region, page)
                    call.respond(matches)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }

            get("/event/{eventId}") {
                val eventId = call.parameters["eventId"]
                if (eventId == null) {
                    call.respondText("Missing event ID", status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val matches = dataService.getMatchesByEvent(eventId, page)
                    call.respond(matches)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }
        }

        // ============ NEWS ENDPOINT ============

        route("/news") {
            get {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val news = dataService.getNews(page)
                    call.respond(news)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }

            get("/details") {
                val url = call.request.queryParameters["url"]
                if (url == null) {
                    call.respondText("Missing 'url' query parameter", status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val newsDetail = dataService.getNewsDetail(url)
                    call.respond(newsDetail)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }
        }

        // ============ TRANSFERS ENDPOINT ============

        get("/transfers") {
            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val transfers = dataService.getTransfers(page)
                call.respond(transfers)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }

        // ============ EVENTS ENDPOINT ============

        get("/events") {
            try {
                val events = dataService.getEvents()
                call.respond(events)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }

        // ============ RANKINGS ENDPOINT ============

        get("/rankings") {
            try {
                val rankings = dataService.getGlobalRankings()
                call.respond(rankings)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }

        // ============ STATS ENDPOINT ============

        get("/stats") {
            try {
                val stats = dataService.getStats()
                call.respond(stats)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ TEAM ENDPOINT ============
        
        get("/team/{id}") {
            val teamId = call.parameters["id"]
            if (teamId == null) {
                call.respondText("Missing team ID", status = HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val team = dataService.getTeam(teamId)
                call.respond(team)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ PLAYER ENDPOINT ============
        
        get("/player/{id}") {
            val playerId = call.parameters["id"]
            if (playerId == null) {
                call.respondText("Missing player ID", status = HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val player = dataService.getPlayer(playerId)
                call.respond(player)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ EVENT DETAIL ENDPOINT ============

        get("/event/{id}") {
            val eventId = call.parameters["id"]
            if (eventId == null) {
                call.respondText("Missing event ID", status = HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val eventDetail = dataService.getEventDetail(eventId)
                call.respond(eventDetail)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ SEARCH ENDPOINTS ============
        
        route("/search") {
            get("/teams") {
                val query = call.request.queryParameters["q"]
                if (query == null) {
                    call.respondText("Missing 'q' query parameter", status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val results = dataService.searchTeams(query)
                    call.respond(results)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }
            
            get("/players") {
                val query = call.request.queryParameters["q"]
                if (query == null) {
                    call.respondText("Missing 'q' query parameter", status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val results = dataService.searchPlayers(query)
                    call.respond(results)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }
        }
        
        // ============ FILTER ENDPOINTS ============
        
        get("/rankings/region/{region}") {
            val region = call.parameters["region"]
            if (region == null) {
                call.respondText("Missing region parameter", status = HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val rankings = dataService.getRankingsByRegion(region)
                call.respond(rankings)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }
        
        // ============ STREAM ENDPOINTS ============
        
        get("/streams") {
            val matchUrl = call.request.queryParameters["url"]
            if (matchUrl == null) {
                call.respondText("Missing 'url' query parameter", status = HttpStatusCode.BadRequest)
                return@get
            }
            try {
                val streams = dataService.getMatchStreams(matchUrl)
                call.respond(streams)
            } catch (e: Exception) {
                call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}
