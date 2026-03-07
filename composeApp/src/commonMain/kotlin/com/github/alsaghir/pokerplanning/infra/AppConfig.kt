package com.github.alsaghir.pokerplanning.infra


sealed interface AppConfig {


    companion object {
        fun fromEnvironment(env: String): AppConfig = when (env.lowercase()) {
            "dev" -> Dev
            "prod" -> Prod
            else -> error("Unknown environment: $env. Allowed: dev, prod")
        }
    }


    val firebaseBaseUrl: String
    val projectId: String

    object Dev : AppConfig {
        override val firebaseBaseUrl: String =
            "http://localhost:8080"
        override val projectId: String = "demo-project"
    }

    object Prod : AppConfig {
        override val firebaseBaseUrl: String =
            "https://googleapis.com"
        override val projectId: String = "poker-planning-fa6af"
    }
}