package io.domil.store.networking

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient {
    return HttpClient {

        engine {
            pipelining = true
        }

        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
            )
        }
        install(HttpTimeout) {
            socketTimeoutMillis = 60000
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 60000
        }
    }
}