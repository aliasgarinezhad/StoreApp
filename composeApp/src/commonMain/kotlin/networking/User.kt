package networking

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var accessToken: String?,
    var username: String ?,
    var fullName: String?,
    var locationCode: Int ?
)
