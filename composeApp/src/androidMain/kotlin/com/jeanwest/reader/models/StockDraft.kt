package com.jeanwest.reader.models

/**
 * Represents a draft of a stock movement operation.  This data class holds information about a pending stock transaction,
 * such as transfers between locations, adjustments, or other operations that affect inventory.  It tracks the details
 * of the draft, including associated products, quantities, and metadata.
 *
 * @property number A unique identifier for the stock draft (Long, defaults to 0L).
 * @property date The date the stock draft was created (String, defaults to "").
 * @property numberOfItems The total number of items (products) included in the draft (Int, defaults to 0).
 * @property source An identifier for the source location of the stock movement (Int, defaults to 0).
 * @property sourceTitle A human-readable title for the source location (String, defaults to "").
 * @property destination An identifier for the destination location of the stock movement (Int, defaults to 0).
 * @property destinationTitle A human-readable title for the destination location (String, defaults to "").
 * @property barcodeTable A list of barcodes associated with the items in the stock draft (MutableList<String>, defaults to empty list).
 * @property specification Additional notes or details about the stock draft (String, defaults to "").
 * @property isConfirmed A flag indicating whether the draft has been confirmed and finalized (Boolean, defaults to false).
 * @property epcsToPrimaryKeysMap A mapping of Electronic Product Code (EPC) values to their corresponding primary keys (MutableMap<String, Long>, defaults to empty map).  This helps to identify products based on their unique EPC.
 * @property logisticKey A key or identifier related to the logistics of the stock movement (String, defaults to "").
 * @property stateID An identifier representing the current state or status of the stock draft (String, defaults to "0").
 * @property positionID An identifier for the position or location within the stock movement process (String, defaults to "").
 * @property logisticDeliverDate The expected or actual date of delivery for the logistics of the stock movement (String, defaults to "").
 * @property createDate The date the stock draft was initially created (String, defaults to "").  May be different from the `date` property.
 * @property logisticGetDate The expected or actual date of pick-up for the logistics of the stock movement (String, defaults to "").
 * @property confirmDate The date the stock */
data class StockDraft(
    val number: Long = 0L,
    val date: String = "",
    var numberOfItems: Int = 0,
    val source: Int = 0,
    val sourceTitle: String = "",
    val destination: Int = 0,
    val destinationTitle: String = "",
    val barcodeTable: MutableList<String> = mutableListOf(),
    val specification: String = "",
    var isConfirmed: Boolean = false,
    var epcsToPrimaryKeysMap: MutableMap<String, Long> = mutableMapOf(),
    var logisticKey: String = "",
    var stateID: String = "0",
    val positionID: String = "",
    val logisticDeliverDate: String = "",
    val createDate: String = "",
    val logisticGetDate: String = "",
    val confirmDate: String = "",
    val driverOperationId: String = "",
    var isScanned: Boolean = false,
    var driver: Int = 0,
    var car: Int = 0,
    var isTwoShel : Boolean = false,
    val stockDraftList : MutableList<StockDraft> = mutableListOf(),
    val items: Map<String, Product> = mapOf()
)