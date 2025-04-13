package com.jeanwest.reader.models

/**
 * Data class representing a Banimode return feature transaction.
 *
 * @property id Unique identifier for the return.
 * @property saleDate Date of the original sale.  Format should be consistent (e.g., "YYYY-MM-DD").
 * @property mobile Mobile number of the customer.
 * @property address Address of the customer.
 * @property isCollectable Indicates if the return is ready to be collected.  Use a convention (e.g., 1 for true, 0 for false, null if unknown).
 * @property isReleasable Indicates if the return can be released (e.g., from storage).  Use a convention (e.g., 1 for true, 0 for false, null if unknown).
 * @property canFinalize Indicates if the return process is complete (e.g., return accepted, refund processed).  Use a convention (e.g., 1 for true, 0 for false, null if unknown).
 * @property color Color of the returned item.
 * @property fullName Full name of the customer.
 * @property saleDesc Description of the original sale (e.g., product name, order details).
 * @property paymentPrice Price paid for the returned item.  Should be in the base currency (e.g., cents if the main currency is dollars).  Null indicates unknown or not applicable.
 */
data class BaniReturn(
    var id: String = "",
    var saleDate: String = "",
    var mobile: String = "",
    var address: String = "",
    var isCollectable: Int? = null,
    var isReleasable: Int? = null,
    var canFinalize: Int? = null,
    var color: String = "",
    var fullName: String = "",
    var saleDesc: String = "",
    var paymentPrice: Long? = null,
)