package com.github.alsaghir.pokerplanning.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.prefs.Preferences

class JvmStorage : Storage {
    private val prefs = Preferences.userNodeForPackage(JvmStorage::class.java)

    override fun getString(key: String): String? = prefs.get(key, null)

    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs.put(key, value)
        prefs.flush()
    }

    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        prefs.remove(key)
        prefs.flush()
    }

}

actual fun createPlatformStorage(): Storage = JvmStorage()