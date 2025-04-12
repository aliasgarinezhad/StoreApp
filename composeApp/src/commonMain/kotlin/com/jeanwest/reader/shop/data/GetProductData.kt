package com.jeanwest.reader.shop.data

import com.jeanwest.reader.data.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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

/**
 * This class is responsible for fetching product-related data from a remote server.
 * It uses Ktor's HttpClient for making network requests and handles various network errors.
 *
 * @property user The user object containing authentication information (e.g., access token).
 * @property httpClient The Ktor HttpClient instance used for making network requests.  It is expected to be pre-configured, likely with a JSON content negotiator.  Example:
 * ```
 * val httpClient = HttpClient {
 *     install(ContentNegotiation) {
 *         json(Json { ignoreUnknownKeys = true })
 *     }
 * }
 * ```
 */
class GetProductData(
    private val user: User,
    private val httpClient: HttpClient
) {

    suspend fun getSimilarProductsByBarcode(
        barcode: String,
        depId: Int
    ): com.jeanwest.reader.data.Result<List<Product>, NetworkError> {


        val response = try {
            httpClient.get(
                urlString = "$severAddress/products/similars/localdb/v2?DepartmentInfo_ID=$depId&kbarcode=$barcode"
            ) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                val response =
                    Json.decodeFromJsonElement<List<Product>>(response.body<JsonObject>()["products"]!!)
                println("response navid ${response.toList()}")
                com.jeanwest.reader.data.Result.Success(response)
            }

            401 -> com.jeanwest.reader.data.Result.Error(NetworkError.UNAUTHORIZED)
            409 -> com.jeanwest.reader.data.Result.Error(NetworkError.CONFLICT)
            408 -> com.jeanwest.reader.data.Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> com.jeanwest.reader.data.Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> com.jeanwest.reader.data.Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                com.jeanwest.reader.data.Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun getSimilarProductsBySearchCode(
        searchCode: String,
        depId: Int
    ): com.jeanwest.reader.data.Result<List<Product>, NetworkError> {
        val response = try {
            httpClient.get(
                urlString = "$severAddress/products/similars/localdb?DepartmentInfo_ID=$depId&&K_Bar_Code=$searchCode"
            ) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${user.accessToken}")
                println("Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                val respone =
                    Json.decodeFromJsonElement<List<Product>>(response.body<JsonObject>()["products"]!!)
                println("response navid ${respone.toList()}")
                com.jeanwest.reader.data.Result.Success(respone)
            }

            401 -> com.jeanwest.reader.data.Result.Error(NetworkError.UNAUTHORIZED)
            409 -> com.jeanwest.reader.data.Result.Error(NetworkError.CONFLICT)
            408 -> com.jeanwest.reader.data.Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> com.jeanwest.reader.data.Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> com.jeanwest.reader.data.Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                com.jeanwest.reader.data.Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun getImageAlbumUrl(
        barcode: String,
    ): com.jeanwest.reader.data.Result<List<String>, NetworkError> {
        val response = try {
            httpClient.get(
                urlString = "$severAddress/products/gallery?KBarCode=$barcode"
            ) {
                contentType(ContentType.Application.Json)
                println("token: "+ user.accessToken)
                header("Authorization", "Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                // Parse the response body
                val responseBody: String = response.body() // Response as a raw string
                val imageUrls: List<String> = Json.decodeFromString(responseBody)

                println("Parsed response: $imageUrls")
                com.jeanwest.reader.data.Result.Success(imageUrls)
            }

            401 -> com.jeanwest.reader.data.Result.Error(NetworkError.UNAUTHORIZED)
            409 -> com.jeanwest.reader.data.Result.Error(NetworkError.CONFLICT)
            408 -> com.jeanwest.reader.data.Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> com.jeanwest.reader.data.Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> com.jeanwest.reader.data.Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                com.jeanwest.reader.data.Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun getScannedProductProperties(
        productCode: String
    ): com.jeanwest.reader.data.Result<List<Product>, NetworkError> {
        val bodyMap = mapOf("KBarCodes" to JsonArray(listOf(JsonPrimitive(productCode))))
        val body = JsonObject(bodyMap)
        println("request body = $body")

        val response = try {
            httpClient.post(
                urlString = "$severAddress/products/v4"
            ) {
                contentType(ContentType.Application.Json)
                setBody(body)
                header("Authorization", "Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        } finally {
            httpClient.close()
        }

        return when (response.status.value) {
            in 200..299 -> {
                val response =
                    Json.decodeFromJsonElement<List<Product>>(response.body<JsonObject>()["KBarCodes"]!!)
                com.jeanwest.reader.data.Result.Success(response)
            }

            401 -> com.jeanwest.reader.data.Result.Error(NetworkError.UNAUTHORIZED)
            409 -> com.jeanwest.reader.data.Result.Error(NetworkError.CONFLICT)
            408 -> com.jeanwest.reader.data.Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> com.jeanwest.reader.data.Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> com.jeanwest.reader.data.Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                com.jeanwest.reader.data.Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun loginUser(
        userName: String,
        password: String
    ): com.jeanwest.reader.data.Result<User, NetworkError> {
        val response = try {
            httpClient.post(
                urlString = "$severAddress/login"
            ) {
                val bodyMap = mutableMapOf(
                    "username" to JsonPrimitive(userName),
                    "password" to JsonPrimitive(password)
                )
                val body = JsonObject(bodyMap)
                setBody(body)
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return com.jeanwest.reader.data.Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                println("response = ${response.body<JsonObject>()}")
                val json = Json{ignoreUnknownKeys = true}
                val response = json.decodeFromJsonElement<User>(response.body<JsonObject>())
                com.jeanwest.reader.data.Result.Success(data = response)
            }

            401 -> com.jeanwest.reader.data.Result.Error(NetworkError.UNAUTHORIZED)
            409 -> com.jeanwest.reader.data.Result.Error(NetworkError.CONFLICT)
            408 -> com.jeanwest.reader.data.Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> com.jeanwest.reader.data.Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> com.jeanwest.reader.data.Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                com.jeanwest.reader.data.Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

}