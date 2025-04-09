package io.domil.store.networking

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val httpClient = createHttpClient()

/**
 * Creates and configures an [HttpClient] for making HTTP requests.
 *
 * The client is configured with the following:
 *  - **Pipelining:**  Enabled for improved performance by allowing multiple requests to be sent without waiting for each response.
 *  - **Logging:**  Logs all request and response details at the [LogLevel.ALL] level for debugging purposes.
 *  - **Content Negotiation:** Configured to handle JSON serialization and deserialization using `kotlinx.serialization`.
 *     - `ignoreUnknownKeys`:  Ignores unknown keys during JSON deserialization to handle potential schema variations.
 *     - `explicitNulls = false`:  Instructs the serializer to omit properties with null values during serialization.
 *  - **Timeouts:** Sets various timeout parameters to prevent requests from hanging indefinitely:
 *     - `socketTimeoutMillis`: Maximum time (in milliseconds) allowed for no activity on the socket.
 *     - `requestTimeoutMillis`: Maximum total time (in milliseconds) allowed for the entire request, including retries.
 *     - `connectTimeoutMillis`: Maximum time (in milliseconds) allowed to establish a connection to the server.
 *
 * @return A configured [HttpClient] instance.
 */
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