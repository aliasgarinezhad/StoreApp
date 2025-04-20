package com.jeanwest.reader.models

import com.jeanwest.reader.useCases.EncodingType

/**
 * Represents an Electronic Product Code (EPC), a unique identifier for physical objects.
 *
 * This data class encapsulates various components of an EPC, including:
 *  - Header: Indicates the length and encoding scheme of the EPC.
 *  - Filter:  Identifies the type of object or application of the tag.
 *  - Partition: Specifies the length of the company prefix.
 *  - Company: A unique identifier assigned to a specific company.
 *  - Item:  An identifier for a specific item or product within the company.
 *  - Serial:  A unique serial number for an individual instance of the item.
 *  - StyleCode: A string representing a specific style of product.
 *  - Color:  A string representing the color of the product.
 *  - Size:  A string representing the size of the product.
 *  - SearchCode:  A code used for searching or looking up product information.
 *  - encodingTypeString: A string representation of the encoding type.
 *  - encodingType:  An enum representing the encoding type used (e.g., AVAKATAN).
 *
 *  Note:  The specific meaning and format of these components depend on the EPC encoding scheme used (indicated by `encodingType`).
 */
data class EPC(
    var header: Int = 0,
    var filter: Int = 0,
    var partition: Int = 0,
    var company: Int = 0,
    var item: Long = 0L,
    var serial: Long = 0L,
    var styleCode: String = "",
    var color: String = "",
    var size: String = "",
    var searchCode: String = "",
    var encodingTypeString: String = "",
    var encodingType: EncodingType = EncodingType.AVAKATAN
)

