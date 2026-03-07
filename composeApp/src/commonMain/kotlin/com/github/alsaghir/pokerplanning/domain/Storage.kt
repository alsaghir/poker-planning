package com.github.alsaghir.pokerplanning.domain

interface Storage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

expect fun createPlatformStorage(): Storage