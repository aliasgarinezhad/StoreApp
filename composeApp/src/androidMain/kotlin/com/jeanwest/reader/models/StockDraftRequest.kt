package com.jeanwest.reader.models

/**
 * Represents a stock draft request, containing information about requested, found, and controlled items,
 * along with details about the source, destination, and user involved.
 *
 * @property number A unique identifier for the stock draft request. Defaults to 0L.
 * @property date The date of the stock draft request as a string (format unspecified). Defaults to "".
 * @property createDateJalali The Jalali (Persian) date of creation as a string. Defaults to "".
 * @property sumOfRequestedItems The total number of items requested in the draft. Defaults to 0.
 * @property sumOfFoundItems The total number of items found during stocktaking. Defaults to 0.
 * @property sumOfControlledItems The total number of items controlled (verified/checked). Defaults to 0.
 * @property source The source location of the stock draft. Defaults to "".
 * @property destination The destination location of the stock draft. Defaults to "".
 * @property specification Additional specifications or notes about the request. Defaults to "".
 * @property collectorName The name of the person who collected the items. Defaults to "".
 * @property user The ID of the user associated with the request, can be null if no user is assigned. Defaults to null.
 * @property stateId The ID of the state of the stock draft request (e.g., pending, approved). Defaults to 0.
 * @property items A list of [StockDraftRequestItem] representing individual items in the request. Defaults to an empty list.
 * @property itemsDistinctByKBarcode A map of [StockDraftRequestItem] keyed by their KBarcode, providing a distinct view of items based on KBarcode. Defaults to an empty map.
 *
 *  @property sumOfNotFoundItems Calculated property representing the difference between the total requested items and the items found.
 *  @property sumOfNotControlledItems Calculated property representing the difference between the total requested items and the items controlled.
 *  @property isCreateByRfid Calculated property indicating whether the draft was likely created using RFID, based on whether the number of EPCs for each item matches the requested number.
 */
data class StockDraftRequest(
    var number: Long = 0L,
    var date: String = "",
    var createDateJalali: String = "",
    var sumOfRequestedItems: Int = 0,
    val sumOfFoundItems: Int = 0,
    val sumOfControlledItems: Int = 0,
    var source: String = "",
    var destination: String = "",
    var specification: String = "",
    var collectorName: String = "",
    var user: Int? = null,
    var stateId: Int = 0,
    var items: List<StockDraftRequestItem> = listOf(),
    var itemsDistinctByKBarcode: Map<String, StockDraftRequestItem> = mapOf(),
) {
    val sumOfNotFoundItems: Int
        get() = sumOfRequestedItems - sumOfFoundItems

    val sumOfNotControlledItems: Int
        get() = sumOfRequestedItems - sumOfControlledItems

    val isCreateByRfid: Boolean
        get() = items.all { it.epcs.size == it.requestNumber }

}