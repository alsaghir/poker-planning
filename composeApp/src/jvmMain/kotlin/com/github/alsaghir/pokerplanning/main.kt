package com.github.alsaghir.pokerplanning

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.alsaghir.pokerplanning.presentation.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PokerPlanning",
    ) {
        App()
    }
}