package com.vlr.scrapper.plugins

import com.vlr.scrapper.routing.theSpikeRoutes
import com.vlr.scrapper.routing.vlrRoutes
import com.vlr.scrapper.services.VlrDataService
import com.vlr.scrapper.clients.TheSpikeClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*

fun Application.configureRouting(
    vlrDataService: VlrDataService,
    theSpikeClient: TheSpikeClient
) {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        get("/") {
            call.respondText("VLR Scraper API is running! Check /swagger for documentation.")
        }
        
        vlrRoutes(vlrDataService)
        theSpikeRoutes(theSpikeClient)
    }
}
