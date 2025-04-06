package io.domil.store.factory.addTaskFeature.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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