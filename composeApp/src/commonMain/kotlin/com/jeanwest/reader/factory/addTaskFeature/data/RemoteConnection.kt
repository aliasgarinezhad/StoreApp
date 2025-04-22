package com.jeanwest.reader.factory.addTaskFeature.data

import com.jeanwest.reader.factory.addTaskFeature.model.ProductionOrder
import com.jeanwest.reader.factory.addTaskFeature.model.UserTask
import com.jeanwest.reader.data.httpClient
import com.jeanwest.reader.data.NetworkError
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import severAddress

/**
 * Object responsible for handling remote connections to the server.
 * It provides methods for user login, retrieving production orders, and submitting user actions.
 */
object RemoteConnection {

    var factoryUser = FactoryUser()

    suspend fun loginUserFactory(
        username: Long,
        password: Long
    ): com.jeanwest.reader.data.Result<FactoryUser, NetworkError> {
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
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        }
        response.status.description
        return when (response.status.value) {
            in 200..299 -> {
                val json = Json { ignoreUnknownKeys = true }
                val jsonBody = response.body<JsonObject>()
                val userFactory = json.decodeFromJsonElement<FactoryUser>(jsonBody)
                factoryUser = userFactory
                com.jeanwest.reader.data.Result.Success(data = userFactory)
            }

            401 -> com.jeanwest.reader.data.Result.Error(NetworkError.UNAUTHORIZED)
            409 -> com.jeanwest.reader.data.Result.Error(NetworkError.CONFLICT)
            408 -> com.jeanwest.reader.data.Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> com.jeanwest.reader.data.Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> com.jeanwest.reader.data.Result.Error(NetworkError.SERVER_ERROR)
            else -> com.jeanwest.reader.data.Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun getProductionOrders(): com.jeanwest.reader.data.Result<List<ProductionOrder>, NetworkError> {
        val response: HttpResponse = try {
            httpClient.get(urlString = "$severAddress/sewing/production-plan") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${factoryUser.accessToken}")
                println("navid body: $$body")
            }
        } catch (_: UnresolvedAddressException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                val json = Json { ignoreUnknownKeys = true }
                val jsonBody = response.body<JsonArray>()
                val productionOrders = json.decodeFromJsonElement<List<ProductionOrder>>(jsonBody)
                com.jeanwest.reader.data.Result.Success(data = productionOrders)
            }

            401 -> com.jeanwest.reader.data.Result.Error(NetworkError.UNAUTHORIZED)
            409 -> com.jeanwest.reader.data.Result.Error(NetworkError.CONFLICT)
            408 -> com.jeanwest.reader.data.Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> com.jeanwest.reader.data.Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> com.jeanwest.reader.data.Result.Error(NetworkError.SERVER_ERROR)
            else -> com.jeanwest.reader.data.Result.Error(NetworkError.UNKNOWN)
        }
    }

    suspend fun finalUserAction(userTask: UserTask): com.jeanwest.reader.data.Result<String, NetworkError> {
        val response: HttpResponse = try {
            httpClient.post(urlString = "$severAddress/sewing/production-plan/action") {

                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val startTime = LocalTime(hour = userTask.startHour, minute = userTask.startMinute)
                val endTime = LocalTime(hour = userTask.endHour, minute = userTask.endMinute)
                val startDate = LocalDateTime(date = today, time = startTime)
                val endDate = LocalDateTime(date = today, time = endTime)
                val dateFormat = LocalDateTime.Format {
                    year()
                    char('-')
                    monthNumber()
                    char('-')
                    dayOfMonth()
                    char(' ')
                    hour()
                    char(':')
                    minute()
                    char(':')
                    second()
                    char('.')
                    char('0')
                    char('0')
                    char('0')
                }

                val bodyMap = mapOf(
                    "ProductionOrderID" to JsonPrimitive(userTask.product.lineID),
                    "ProductionOrderOperationID" to JsonPrimitive(userTask.taskId),
                    "StyleNumber" to JsonPrimitive(userTask.product.style),
                    "ColorCode" to JsonPrimitive(userTask.product.color),
                    "Part" to JsonPrimitive(userTask.product.part),
                    "SizeCode" to JsonPrimitive(userTask.sizeCode),
                    "Quantity" to JsonPrimitive(userTask.number),
                    "StartDate" to JsonPrimitive(startDate.format(dateFormat)),
                    "EndDate" to JsonPrimitive(endDate.format(dateFormat)),
                    "MachineCode" to JsonPrimitive(userTask.machineCode),
                )
                val body = JsonObject(bodyMap)
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${factoryUser.accessToken}")
                setBody(body)
                println("navid body: $body")
            }
        } catch (_: UnresolvedAddressException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                com.jeanwest.reader.data.Result.Success(data = "OK")
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