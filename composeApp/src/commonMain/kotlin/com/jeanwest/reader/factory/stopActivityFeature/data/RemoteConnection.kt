package com.jeanwest.reader.factory.stopActivityFeature.data

import com.jeanwest.reader.factory.addTaskFeature.data.RemoteConnection.factoryUser
import com.jeanwest.reader.data.httpClient
import com.jeanwest.reader.data.NetworkError
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

/**
 *  A singleton object responsible for handling remote connections and API calls related to machine control.
 */
object RemoteConnection {

    suspend fun stopActivity(
        machineId: Int,
        reasonId: Int
    ): com.jeanwest.reader.data.Result<String, NetworkError> {
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
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        }
        response.status.description
        return when (response.status.value) {
            in 200..299 -> {
                com.jeanwest.reader.data.Result.Success(data = "Ok")
            }

            401 -> com.jeanwest.reader.data.Result.Error(NetworkError.UNAUTHORIZED)
            409 -> com.jeanwest.reader.data.Result.Error(NetworkError.CONFLICT)
            408 -> com.jeanwest.reader.data.Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> com.jeanwest.reader.data.Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> com.jeanwest.reader.data.Result.Error(NetworkError.SERVER_ERROR)
            else -> com.jeanwest.reader.data.Result.Error(NetworkError.UNKNOWN)
        }
    }

}