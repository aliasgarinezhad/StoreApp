package io.domil.store.factory.addTaskFeature.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a factory user with their associated information.
 *
 * @property fullName The full name of the factory user. Defaults to an empty string.  This field is serialized as "FullName" in the JSON representation.
 * @property accessToken The access token used for authentication and authorization for this user.  Defaults to an empty string.
 * @property icons A list of icons associated with the user, represented as a list of [Icon] objects. Defaults to an empty list.
 * @property machineCode An optional integer representing the machine code assigned to the user.  This field is serialized as "MachineCode" in the JSON representation. Can be null if no machine code is assigned.
 */
@Serializable
data class FactoryUser(
    @SerialName("FullName")
    val fullName: String = "",
    val accessToken: String = "",
    val icons: List<Icon> = emptyList(),
    @SerialName("MachineCode")
    val machineCode: Int? = null
)

@Serializable
data class Icon(
    @SerialName("IconName")
    val iconName: String,
    @SerialName("IconLatinName")
    val iconLatinName: String,
    @SerialName("IconStatus")
    val iconStatus: Boolean,
)