package com.github.alsaghir.pokerplanning.domain

import java.util.prefs.Preferences

class JvmStorage : Storage {
    private val prefs = Preferences.userNodeForPackage(JvmStorage::class.java)

    override fun getString(key: String): String? = prefs.get(key, null)

    override suspend fun putString(key: String, value: String) {
        prefs.put(key, value)
        prefs.flush()
    }

    override fun remove(key: String) {
        prefs.remove(key)
        prefs.flush()
    }
}

actual fun createPlatformStorage(): Storage = JvmStorage()