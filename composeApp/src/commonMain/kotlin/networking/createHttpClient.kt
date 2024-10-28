package networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.timeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient {
    return HttpClient(CIO) {

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