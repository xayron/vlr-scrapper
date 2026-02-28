package com.vlr.scrapper.services

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

class RedisService(redisUrl: String) {
    private val client = RedisClient.create(redisUrl)
    private val connection = client.connect()
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    val commands: RedisCoroutinesCommands<String, String> = connection.coroutines()

    fun close() {
        connection.close()
        client.shutdown()
    }
}