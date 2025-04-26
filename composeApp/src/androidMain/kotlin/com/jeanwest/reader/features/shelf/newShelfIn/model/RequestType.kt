package com.jeanwest.reader.features.shelf.newShelfIn.model

/**
 * Enum class representing the type of request.
 *
 *  This enum is used to differentiate between two types of return requests:
 *  - Product: Represents a return request for a specific product.  The `toString()` representation is "دریافت مرجوعی" (Receive Return).
 *  - Carton: Represents a request related to carton sizing. The `toString()` representation is "سایز بندی" (Sizing).
 *
 *  The `toString()` method provides a user-friendly, localized string representation of the request type, suitable for display in a UI.
 */
enum class RequestType {
    Product,
    Carton;

    override fun toString(): String {
        return if (this == Product) "دریافت مرجوعی" else "سایز بندی"
    }
}