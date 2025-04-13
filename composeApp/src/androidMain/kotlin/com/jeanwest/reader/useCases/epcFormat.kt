package com.jeanwest.reader.useCases

import com.jeanwest.reader.models.EPC


/**
 * Enum class representing different types of encodings.
 *
 * This enum is used to categorize and identify the specific encoding schemes used for processing or storing data.
 * Each enum value corresponds to a distinct encoding method.
 *
 *  - **AVAKATAN**: Represents the AVAKATAN encoding type, specifics of which are not detailed here and depend on the broader application context.
 *  - **primaryLight**: Represents the "primaryLight" encoding type.  The meaning and characteristics of this encoding depend on the application using it, but the name suggests it might be a lighter or simplified version compared to others.
 *  - **JOOTIJEANS**: Represents the "JOOTIJEANS" encoding type. Again, the specific details of this encoding method are not defined here and would be understood within the context of its usage.  The unusual capitalization suggests it might be a specific, potentially trademarked, or custom encoding name.
 */
enum class EncodingType {
    AVAKATAN,
    primaryLight,
    JOOTIJEANS
}

/**
 * Generates an Electronic Product Code (EPC) as a hexadecimal string.
 *
 * This function constructs an EPC based on the provided item number and serial number,
 * incorporating predefined header, filter, partition, and company values.  The EPC
 * conforms to a 96-bit structure, represented as a 24-character hexadecimal string.
 *
 * The EPC is constructed as follows:
 * - **Header (8 bits):**  Indicates the EPC scheme (SGTIN-96 in this case).
 * - **Partition (3 bits):**  Defines the bit allocation for the company prefix and item reference.
 * - **Filter (3 bits):**  Specifies the type of item or application.
 * - **Company Prefix (12 bits):**  A unique identifier for the manufacturing company.
 * - **Item Reference (32 bits):**  A unique identifier for the specific item within the company's product range.
 * - **Serial Number (38 bits):**  A unique serial number for the individual item.
 *
 * The function performs the following steps:
 * 1. Converts each component (header, filter, partition, company, item, serial) to its binary representation.
 * 2. Pads the binary strings with leading zeros to achieve the required bit lengths.
 * 3. Concatenates the binary strings to form the complete 96-bit EPC.
 * 4. Converts the first 64 bits of the binary EPC to a 16-character hexadecimal string.
 * 5. Converts the last 32 bits of the binary EPC to an 8-character hexadecimal string.
 * 6. Concatenates the two hexadecimal strings to produce the final 24-character EPC.
 *
 * @param item The item number (up to 32 bits).
 * @param serial The serial number (up to 38 bits).
 * @return The generated EPC as a 24-character hexadecimal string.
 *
 * @throws IllegalArgumentException if the provided item or serial exceeds their maximum bit lengths.
 */
fun epcGenerator(
    item: Long,
    serial: Long,
): String {

    val header = 48
    val filter = 0
    val partition = 0
    val company = 101

    var tempStr = java.lang.Long.toBinaryString(header.toLong())
    val headerStr = String.format("%8s", tempStr).replace(" ".toRegex(), "0")
    tempStr = java.lang.Long.toBinaryString(filter.toLong())
    val filterStr = String.format("%3s", tempStr).replace(" ".toRegex(), "0")
    tempStr = java.lang.Long.toBinaryString(partition.toLong())
    val positionStr = String.format("%3s", tempStr).replace(" ".toRegex(), "0")
    tempStr = java.lang.Long.toBinaryString(company.toLong())
    val companynumberStr = String.format("%12s", tempStr).replace(" ".toRegex(), "0")
    tempStr = java.lang.Long.toBinaryString(item)
    val itemNumberStr = String.format("%32s", tempStr).replace(" ".toRegex(), "0")
    tempStr = java.lang.Long.toBinaryString(serial)
    val serialNumberStr = String.format("%38s", tempStr).replace(" ".toRegex(), "0")
    val epcStr =
        headerStr + positionStr + filterStr + companynumberStr + itemNumberStr + serialNumberStr // binary string of EPC (96 bit)

    tempStr = epcStr.substring(0, 64).toULong(2).toString(16)
    val epc0To64 = String.format("%16s", tempStr).replace(" ".toRegex(), "0")
    tempStr = epcStr.substring(64, 96).toULong(2).toString(16)
    val epc64To96 = String.format("%8s", tempStr).replace(" ".toRegex(), "0")

    return epc0To64 + epc64To96

}

/**
 * Decodes an Electronic Product Code (EPC) string into an [EPC] object.
 *
 * The function supports decoding of EPCs with different encoding schemes based on the prefix of the EPC string:
 * - "30": AVAKATAN encoding. The EPC is parsed into header, partition, filter, company, item, and serial number components.
 * - "00": JOOTIJEANS encoding. The EPC is interpreted as a decimal item number, further divided into style code, color, and size.
 * - "1": Primary Light encoding. The EPC is directly used as a search code.
 *
 * @param epc The EPC string to decode.  Must be a hexadecimal string.
 * @return An [EPC] object representing the decoded EPC, or `null` if the EPC is invalid or uses an unsupported encoding.
 *
 * AVAKATAN Encoding:
 *  - Expects an EPC string starting with "30" and having a length of at least 24 characters.
 *  - The first 16 characters represent a 64-bit binary string (padded with zeros), and the next 8 characters represent a 32-bit binary string.
 *  - These binary strings are then parsed to extract individual components of the EPC.
 *
 * JOOTIJEANS Encoding:
 *  - Expects an EPC string starting with "00".
 *  - The first 16 characters are interpreted as a hexadecimal number, which is then converted to a decimal string.
 *  - If the resulting decimal string has at least 14 digits, it's further parsed into style code (first 8 digits), color (next 4 digits), and size (remaining digits mapped to size codes).
 *
 * Primary Light Encoding:
 *  - Expects an EPC string starting with "1".
 *  - The first 16 characters are directly used as the search code.
 *
 *  Returns `null` if the input string:
 *  - Has a length less than 24 and doesn't start with "00" or "1".
 *  - Doesn't start with a supported prefix ("30", "00", or "1").
 *  - Represents an invalid number in hexadecimal format (leading to an exception during conversion).
 *  - Results in a decimal item */
fun epcDecoder(epc: String): EPC? {

    if (epc.length < 24) {
        return null
    } else if (epc.startsWith("30")) {
        val binaryEPC =
            String.format("%64s", epc.substring(0, 16).toULong(16).toString(2))
                .replace(" ".toRegex(), "0") +
                    String.format("%32s", epc.substring(16, 24).toULong(16).toString(2))
                        .replace(" ".toRegex(), "0")
        val result = EPC(0, 0, 0, 0, 0L, 0L)
        result.header = binaryEPC.substring(0, 8).toInt(2)
        result.partition = binaryEPC.substring(8, 11).toInt(2)
        result.filter = binaryEPC.substring(11, 14).toInt(2)
        result.company = binaryEPC.substring(14, 26).toInt(2)
        result.item = binaryEPC.substring(26, 58).toLong(2)
        result.serial = binaryEPC.substring(58, 96).toLong(2)
        result.encodingTypeString = "avakatan"
        result.encodingType = EncodingType.AVAKATAN
        return result
    } else if (epc.startsWith("00")) {
        val decimalItemNumber = epc.substring(0, 16).toULong(radix = 16).toString()
        val result = EPC()
        return if (decimalItemNumber.length >= 14) {
            result.encodingTypeString = "jootijeans"
            result.encodingType = EncodingType.JOOTIJEANS
            result.styleCode = decimalItemNumber.substring(0, 8)
            result.color = decimalItemNumber.substring(8, 12)
            result.size = when (decimalItemNumber.substring(12, decimalItemNumber.length)) {
                "01" -> "S"
                "02" -> "M"
                "03" -> "L"
                "04" -> "XL"
                "05" -> "XXL"
                "06" -> "XXXL"
                "07" -> "4XL"
                "08" -> "5XL"
                "09" -> "6XL"
                "99" -> "F"
                else -> decimalItemNumber.substring(12, decimalItemNumber.length)
            }
            result
        } else {
            null
        }

    } else if (epc.startsWith("1")) {
        val decimalItemNumber = epc.substring(0, 16)
        val result = EPC()
        result.searchCode = decimalItemNumber
        result.encodingTypeString = "primaryLight"
        result.encodingType = EncodingType.primaryLight
        return result
    } else {
        return null
    }
}