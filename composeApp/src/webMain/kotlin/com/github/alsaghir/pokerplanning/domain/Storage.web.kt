package com.github.alsaghir.pokerplanning.domain

import kotlinx.browser.window

class WebStorage : Storage {
    private val localStorage = window.localStorage

    override fun getString(key: String): String? = localStorage.getItem(key)

    override fun putString(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    override fun remove(key: String) {
        localStorage.removeItem(key)
    }
}

actual fun createPlatformStorage(): Storage = WebStorage()