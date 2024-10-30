package networking

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var token: String ?,
    var username: String ?,
    var userFullName: String ?,
    var locationCode: Int ?
)
