package com.github.alsaghir.pokerplanning.presentation

import kotlinx.serialization.Serializable

@Serializable
object Routes {
    @Serializable
    object Home

    @Serializable
    object Guide

    @Serializable
    object Examples

    @Serializable
    data class GameSession(
        val gameId: String,
        val playerId: String
    )
}