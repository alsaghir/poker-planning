package com.github.alsaghir.pokerplanning

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PokerPlanning",
    ) {
        App()
    }
}