package com.vlr.scrapper.repository

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.SetArgs
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.json.Json

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class CacheRepository (private val redis: RedisCoroutinesCommands<String, String>) {
    suspend fun get(key: String) = redis.get(key)

    suspend fun set(key: String, value: String, args: SetArgs = SetArgs()) = redis.set(key, value ,args)

    suspend fun delete(key: String) = redis.del(key)

    suspend fun invalidate() = redis.flushall()
}