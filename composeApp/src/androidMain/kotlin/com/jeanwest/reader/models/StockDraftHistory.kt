package com.jeanwest.reader.models

/**
 * Represents the history of a stock draft.
 *
 * @property stockDraftID The unique identifier of the stock draft.  Defaults to an empty string.
 * @property fromWareHouseID The identifier of the warehouse the stock is being transferred from. Defaults to an empty string.
 * @property toWareHouseID The identifier of the warehouse the stock is being transferred to. Defaults to an empty string.
 * @property sendDate The date the stock draft was sent.  Stored as a String, format unspecified. Defaults to an empty string.
 * @property createDate The date the stock draft was created. Stored as a String, format unspecified. Defaults to an empty string.
 * @property updateDate The date the stock draft was last updated. Stored as a String, format unspecified. Defaults to an empty string.
 * @property stockDraftStatusID The identifier of the stock draft's status. Defaults to an empty string.
 * @property statusTitle The title or description of the stock draft's status. Defaults to an empty string.
 * @property sumProductQty The total quantity of products included in the stock draft. Defaults to 0.
 * @property positionID  An identifier for a position related to the stock draft (purpose unclear without further context). Defaults to 0.
 * @property stateID  An identifier for the state of the stock draft (purpose unclear without further context). Defaults to 0.
 * @property stockDraftDescription A description or notes about the stock draft. Defaults to an empty string.
 * @property deliveryCode A code associated with the delivery of the stock draft. Defaults to an empty string.
 */
data class StockDraftHistory(
    val stockDraftID: String = "",
    val fromWareHouseID: String = "",
    val toWareHouseID: String = "",
    val sendDate: String = "",
    val createDate: String = "",
    val updateDate: String = "",
    val stockDraftStatusID: String = "",
    val statusTitle: String = "",
    val sumProductQty: Int = 0,
    val positionID: Int = 0,
    val stateID: Int = 0,
    val stockDraftDescription: String = "",
    val deliveryCode: String =""
)