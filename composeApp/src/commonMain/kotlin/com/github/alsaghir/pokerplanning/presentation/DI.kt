package com.github.alsaghir.pokerplanning.presentation

import androidx.compose.ui.graphics.Color
import com.github.alsaghir.pokerplanning.domain.Storage
import com.github.alsaghir.pokerplanning.domain.ThemeRepo
import com.github.alsaghir.pokerplanning.domain.ThemeRepoImpl
import com.github.alsaghir.pokerplanning.domain.createPlatformStorage
import com.github.alsaghir.pokerplanning.infra.AppConfig
import com.github.alsaghir.pokerplanning.presentation.model.ThemeViewModel
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel


val appModule = module {
    single<AppConfig> { AppConfig.fromEnvironment("dev") }

    single<Json> {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    single<Storage> { createPlatformStorage() }

    single<ThemeRepo> {
        val config = get<AppConfig>()
        ThemeRepoImpl(
            storage = get(),
            defaultColor = Color(config.defaultSeedColorValue),
            json = get()
        )
    }

    viewModel<ThemeViewModel>()
}
