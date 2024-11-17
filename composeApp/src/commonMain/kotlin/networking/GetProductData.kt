package networking

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
import util.NetworkError
import util.Result

class GetProductData(
    private val user: User,
    private val httpClient: HttpClient
) {

    suspend fun getSimilarProductsByBarcode(
        barcode: String,
        depId: Int
    ): Result<List<Product>, NetworkError> {

        val response = try {
            httpClient.get(
                urlString = "https://rfid-api.avakatan.ir/products/similars/localdb?DepartmentInfo_ID=$depId&kbarcode=$barcode"
            ) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                val response =
                    Json.decodeFromJsonElement<List<Product>>(response.body<JsonObject>()["products"]!!)
                println("response navid ${response.toList()}")
                Result.Success(response)
            }

            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun getSimilarProductsBySearchCode(
        searchCode: String,
        depId: Int
    ): Result<List<Product>, NetworkError> {
        val response = try {
            httpClient.get(
                urlString = "https://rfid-api.avakatan.ir/products/similars/localdb?DepartmentInfo_ID=$depId&&K_Bar_Code=$searchCode"
            ) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${user.accessToken}")
                println("Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                val respone =
                    Json.decodeFromJsonElement<List<Product>>(response.body<JsonObject>()["products"]!!)
                println("response navid ${respone.toList()}")
                Result.Success(respone)
            }

            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun getImageAlbumUrl(
        barcode: String,
    ): Result<List<String>, NetworkError> {
        val response = try {
            httpClient.get(
                urlString = "https://rfid-api.avakatan.ir/products/gallery?KBarCode=$barcode"
            ) {
                contentType(ContentType.Application.Json)
                println("token: "+ user.accessToken)
                header("Authorization", "Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                // Parse the response body
                val responseBody: String = response.body() // Response as a raw string
                val imageUrls: List<String> = Json.decodeFromString(responseBody)

                println("Parsed response: $imageUrls")
                Result.Success(imageUrls)
            }

            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun getScannedProductProperties(
        productCode: String
    ): Result<List<Product>, NetworkError> {
        val bodyMap = mapOf("KBarCodes" to JsonArray(listOf(JsonPrimitive(productCode))))
        val body = JsonObject(bodyMap)
        println("request body = $body")

        val response = try {
            httpClient.post(
                urlString = "https://rfid-api.avakatan.ir/products/v4"
            ) {
                contentType(ContentType.Application.Json)
                setBody(body)
                header("Authorization", "Bearer ${user.accessToken}")
            }

        } catch (_: UnresolvedAddressException) {
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        } finally {
            httpClient.close()
        }

        return when (response.status.value) {
            in 200..299 -> {
                val response =
                    Json.decodeFromJsonElement<List<Product>>(response.body<JsonObject>()["KBarCodes"]!!)
                Result.Success(response)
            }

            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun loginUser(
        userName: String,
        password: String
    ): Result<User, NetworkError> {
        val response = try {
            httpClient.post(
                urlString = "https://rfid-api.avakatan.ir/login"
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
            return Result.Error(NetworkError.NO_INTERNET)
        } catch (_: SerializationException) {
            return Result.Error(NetworkError.SERIALIZATION)
        }

        return when (response.status.value) {
            in 200..299 -> {
                println("response = ${response.body<JsonObject>()}")
                val json = Json{ignoreUnknownKeys = true}
                val response = json.decodeFromJsonElement<User>(response.body<JsonObject>())
                Result.Success(data = response)
            }

            401 -> Result.Error(NetworkError.UNAUTHORIZED)
            409 -> Result.Error(NetworkError.CONFLICT)
            408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
            413 -> Result.Error(NetworkError.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
            else -> {
                Result.Error(NetworkError.UNKNOWN)
            }
        }
    }

}