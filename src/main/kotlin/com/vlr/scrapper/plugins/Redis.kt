package com.vlr.scrapper.plugins

import com.vlr.scrapper.services.RedisService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.util.AttributeKey

fun Application.configureRedis() {
    val redisUrl = environment.config.property("redis.url").getString()
    val redisService = RedisService(redisUrl)

    // Clean up on shutdown
    environment.monitor.subscribe(ApplicationStopped) {
        redisService.close()
    }

    // Make it available app-wide
    attributes.put(RedisServiceKey, redisService)
}

val RedisServiceKey = AttributeKey<RedisService>("RedisService")