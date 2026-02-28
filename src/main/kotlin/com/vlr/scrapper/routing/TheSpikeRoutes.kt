package com.vlr.scrapper.routing

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode

import com.vlr.scrapper.clients.TheSpikeClient

fun Route.theSpikeRoutes(client: TheSpikeClient) {
    route("/thespike") {
        get("/regions") {
                call.respond(client.getAllRegions())
            }
            
            route("/rankings") {
                get {
                    try {
                        val region = call.request.queryParameters["region"]?.toIntOrNull()
                        val rankings = client.getRankings(region)
                        call.respond(rankings)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
                
                get("/region/{regionId}") {
                    val regionId = call.parameters["regionId"]?.toIntOrNull()
                    if (regionId == null) {
                        call.respondText("Missing or invalid region ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    try {
                        val rankings = client.getRankings(regionId)
                        call.respond(rankings)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
            
            route("/events") {
                get {
                    try {
                        val region = call.request.queryParameters["region"]?.toIntOrNull()
                        val events = client.getEvents(region)
                        call.respond(events)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
                
                get("/region/{regionId}") {
                    val regionId = call.parameters["regionId"]?.toIntOrNull()
                    if (regionId == null) {
                        call.respondText("Missing or invalid region ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    try {
                        val events = client.getEvents(regionId)
                        call.respond(events)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
                
                get("/{eventId}") {
                    val eventId = call.parameters["eventId"]?.toIntOrNull()
                    if (eventId == null) {
                        call.respondText("Missing or invalid event ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    try {
                        val event = client.getEvent(eventId)
                        call.respond(event)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
            
            route("/matches") {
                get {
                    try {
                        val region = call.request.queryParameters["region"]?.toIntOrNull()
                        val matches = client.getMatches(region)
                        call.respond(matches)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
                
                get("/live") {
                    try {
                        val region = call.request.queryParameters["region"]?.toIntOrNull()
                        val matches = client.getLiveMatches(region)
                        call.respond(matches)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
                
                get("/region/{regionId}") {
                    val regionId = call.parameters["regionId"]?.toIntOrNull()
                    if (regionId == null) {
                        call.respondText("Missing or invalid region ID", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    try {
                        val matches = client.getMatches(regionId)
                        call.respond(matches)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
            
            get("/match/{matchId}") {
                val matchId = call.parameters["matchId"]?.toIntOrNull()
                if (matchId == null) {
                    call.respondText("Missing or invalid match ID", status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val match = client.getMatch(matchId)
                    call.respond(match)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }
            
            get("/teams/{teamId}") {
                val teamId = call.parameters["teamId"]?.toIntOrNull()
                if (teamId == null) {
                    call.respondText("Missing or invalid team ID", status = HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val team = client.getTeam(teamId)
                    call.respond(team)
                } catch (e: Exception) {
                    call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }
            
            route("/news") {
                get {
                    try {
                        val page = call.request.queryParameters["page"]?.toIntOrNull()
                        val category = call.request.queryParameters["category"]
                        val country = call.request.queryParameters["country"]
                        val news = client.getNews(page, category, country)
                        call.respond(news)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
                
                get("/category/{category}") {
                    val category = call.parameters["category"]
                    if (category == null) {
                        call.respondText("Missing category parameter", status = HttpStatusCode.BadRequest)
                        return@get
                    }
                    try {
                        val page = call.request.queryParameters["page"]?.toIntOrNull()
                        val news = client.getNews(page, category, null)
                        call.respond(news)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                    }
                }
            }
        }
    }
