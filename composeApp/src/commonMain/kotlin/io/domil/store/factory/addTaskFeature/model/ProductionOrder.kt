package io.domil.store.factory.addTaskFeature.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductionOrder(
    @SerialName("ProductionOrderID")
    val productionOrderId: Long,
    @SerialName("ManufacturingOrderType")
    val manufacturingOrderType: String,
    @SerialName("Number")
    val number: String,
    @SerialName("Date")
    val date: String,
    @SerialName("State")
    val state: Int,
    @SerialName("StateProductionOrder")
    val stateProductionOrder: String,
    @SerialName("PartID")
    val partId: Int,
    @SerialName("Part")
    val part: String,
    @SerialName("PartName")
    val partName: String,
    @SerialName("PartCode")
    val partCode: String,
    @SerialName("StyleN")
    val styleN: String,
    @SerialName("ColorCodeF")
    val colorCodeF: String,
    @SerialName("ColorHEX")
    val colorHex: String,
    @SerialName("OperationItems")
    val operationItems: List<OperationItem>
)

@Serializable
data class OperationItem(
    @SerialName("ProductionOrderOperationID")
    val productionOrderOperationId: Long,
    @SerialName("Operation")
    val operation: String,
    @SerialName("Rank")
    val rank: Int,
    @SerialName("Pieces")
    val pieces: List<Piece>
)

@Serializable
data class Piece(
    @SerialName("SIZE")
    val size: String,
    @SerialName("SizeCode")
    val sizeCode: Int,
    @SerialName("CutQuantity")
    val cutQuantity: Int
)