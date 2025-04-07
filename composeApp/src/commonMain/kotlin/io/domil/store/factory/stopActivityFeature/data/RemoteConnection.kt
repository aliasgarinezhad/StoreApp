package io.domil.store.factory.stopActivityFeature.data

import io.domil.store.factory.addTaskFeature.data.RemoteConnection.factoryUser
import io.domil.store.networking.httpClient
import io.domil.store.tools.NetworkError
import io.domil.store.tools.Result
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import severAddress

object RemoteConnection {

    suspend fun stopActivity(
        machineId: Int,
        reasonId: Int
    ): Result<String, NetworkError> {
        val response: HttpResponse = try {
            httpClient.post(urlString = "$severAddress/sewing/production-plan/action/stop") {
                val bodyMap = mutableMapOf(
                    "MachineID" to JsonPrimitive(machineId),
                    "DamageReasonRef" to JsonPrimitive(reasonId)
                )
                val body = JsonObject(bodyMap)
                println("navid body: $$body")
                header("Authorization", "Bearer ${factoryUser.accessToken}")
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
                Result.Success(data = "Ok")
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