package io.domil.store.factory.addTaskFeature.data

import io.domil.store.factory.addTaskFeature.model.ProductionOrder
import io.domil.store.tools.NetworkError
import io.domil.store.tools.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import severAddress

class RemoteConnection(
    private val httpClient: HttpClient,
 //   private val factoryUser: FactoryUser,
) {

    suspend fun loginUserFactory(
        username: Long,
        password: Long
    ): Result<FactoryUser, NetworkError> {
        val response: HttpResponse = try {
            httpClient.post(urlString = "$severAddress/login/sewing") {
                val bodyMap = mutableMapOf(
                    "username" to JsonPrimitive(username),
                    "password" to JsonPrimitive(password)
                )
                val body = JsonObject(bodyMap)
                println("navid body: $$body")
                setBody(body)
                contentType(ContentType.Application.Json)
            }
        } catch (_: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }
        response.status.description
        return when (response.status.value) {
            in 200..299 -> {
                val json = Json { ignoreUnknownKeys = true }
                val jsonBody = response.body<JsonObject>()
                val userFactory = json.decodeFromJsonElement<FactoryUser>(jsonBody)
                Result.Success(data = userFactory)
            }

            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun getProductionOrders(): Result<List<ProductionOrder>, NetworkError> {
        val response: HttpResponse = try {
            httpClient.get(urlString = "$severAddress/sewing/production-plan") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwIiwibmFtZSI6Itin2YTZh9in2YUg2q_ZhCDZhdit2YXYr9uM2KfZhiDar9mE24zYp9mGIiwicm9sZXMiOlsidXNlciJdLCJzY29wZXMiOlsic2V3aW5nIl0sImlhdCI6MTc0MTY5MTAyMSwibmJmIjoxNzQxNjkxMDIxLCJleHAiOjE3NDIyNzE2MjksImF1ZCI6InJhaGthcmFuIn0.Qidbv1C2tKd8JECxCHOACQGXEE3kRFCMxpaKC2I6VKI")
                println("navid body: $$body")
            }
        } catch (_: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                val json = Json { ignoreUnknownKeys = true }
                val jsonBody = response.body<JsonArray>()
                val productionOrders = json.decodeFromJsonElement<List<ProductionOrder>>(jsonBody)
                Result.Success(data = productionOrders)
            }

            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun finalUserAction(): Result<String, NetworkError> {
        val response: HttpResponse = try {
            httpClient.post(urlString = "$severAddress/sewing/production-plan/action") {
//                val bodyMap = mutableMapOf(
//                    "username" to JsonPrimitive(username),
//                    "password" to JsonPrimitive(password)
//                )
//                val body = JsonObject(bodyMap)
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwIiwibmFtZSI6Itin2YTZh9in2YUg2q_ZhCDZhdit2YXYr9uM2KfZhiDar9mE24zYp9mGIiwicm9sZXMiOlsidXNlciJdLCJzY29wZXMiOlsic2V3aW5nIl0sImlhdCI6MTc0MTY5MTAyMSwibmJmIjoxNzQxNjkxMDIxLCJleHAiOjE3NDIyNzE2MjksImF1ZCI6InJhaGthcmFuIn0.Qidbv1C2tKd8JECxCHOACQGXEE3kRFCMxpaKC2I6VKI")
                println("navid body: $$body")
            }
        } catch (_: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                Result.Success(data = "OK")
            }

            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> Result.Error(NetworkError.UNKNOWN)
        }
    }

}