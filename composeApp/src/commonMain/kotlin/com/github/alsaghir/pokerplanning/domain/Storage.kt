package com.github.alsaghir.pokerplanning.domain

interface Storage {
    fun getString(key: String): String?
    suspend fun putString(key: String, value: String)
    suspend fun remove(key: String)
}

expect fun createPlatformStorage(): Storage