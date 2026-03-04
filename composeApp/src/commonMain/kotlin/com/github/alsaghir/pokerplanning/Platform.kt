package com.github.alsaghir.pokerplanning

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform