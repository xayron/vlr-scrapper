package com.vlr.scrapper

import com.vlr.scrapper.clients.TheSpikeClient
import com.vlr.scrapper.clients.VlrScraperClient
import com.vlr.scrapper.plugins.*
import com.vlr.scrapper.repository.CacheRepository
import com.vlr.scrapper.repository.VlrRepository
import com.vlr.scrapper.services.VlrDataService
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.lettuce.core.ExperimentalLettuceCoroutinesApi

fun main(args: Array<String>): Unit = EngineMain.main(args)

@OptIn(ExperimentalLettuceCoroutinesApi::class)
fun Application.module() {
    configureMonitoring()
    configureSerialization()
    configureCORS()
    configureRedis()

    val vlrScraper = VlrScraperClient()
    val theSpikeClient = TheSpikeClient()
    
    val redisService = attributes[RedisServiceKey]
    val cacheRepository = CacheRepository(redisService.commands)
    
    val vlrDataService = VlrDataService(VlrRepository(vlrScraper), cacheRepository)
    
    configureRouting(vlrDataService, theSpikeClient)
}
