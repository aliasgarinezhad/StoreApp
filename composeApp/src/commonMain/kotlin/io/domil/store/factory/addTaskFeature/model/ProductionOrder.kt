package io.domil.store.factory.addTaskFeature.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a production order in a manufacturing system.
 *
 * @property productionOrderId Unique identifier for the production order.  Represented as "ProductionOrderID" in the serialized form.
 * @property manufacturingOrderType Type of manufacturing order. Represented as "ManufacturingOrderType".
 * @property number Production order number. Represented as "Number".
 * @property date Date the production order was created. Represented as "Date".  Expected format should be documented separately if applicable (e.g., ISO 8601).
 * @property state Numerical representation of the production order's state. Represented as "State". Refer to external documentation for state code definitions.
 * @property stateProductionOrder Textual description of the production order's state. Represented as "StateProductionOrder".
 * @property partId Identifier for the part being produced. Represented as "PartID".
 * @property part Identifier for the part (likely a code or short name). Represented as "Part".
 * @property partName Name of the part being produced. Represented as "PartName".
 * @property partCode Code for the part being produced. Represented as "PartCode".
 * @property styleN Style number associated with the production order. Represented as "StyleN".
 * @property colorCodeF Color code associated with the production order. Represented as "ColorCodeF".
 * @property colorHex Hexadecimal representation of the color. Represented as "ColorHEX".
 * @property operationItems List of [OperationItem] objects representing the operations within the production order.  Represented as "OperationItems".
 */
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