package com.github.alsaghir.pokerplanning.presentation

import androidx.compose.ui.graphics.Color
import com.github.alsaghir.pokerplanning.domain.ThemeRepo
import com.github.alsaghir.pokerplanning.domain.createPlatformStorage
import com.github.alsaghir.pokerplanning.infra.AppConfig
import kotlinx.serialization.json.Json

class AppContainer(private val defaultColor: Color) {

    val appConfig by lazy { AppConfig.fromEnvironment("dev") }

    val json by lazy {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
    val storage by lazy { createPlatformStorage() }
     val themeRepo by lazy { ThemeRepo(storage, defaultColor, json) }

    fun close() {

    }

}

