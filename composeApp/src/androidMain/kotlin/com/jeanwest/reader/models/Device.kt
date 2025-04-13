package com.jeanwest.reader.models

/**
 * Represents a device with its associated information.
 *
 * @property id The unique identifier of the device. Defaults to "null".  An empty string indicates the device does not exist in the system.
 * @property location The geographical location of the device. Defaults to "null".
 * @property locationCode The code representing the location of the device. Defaults to "null".
 * @property token A token used for authentication or authorization with the device. Defaults to "null".
 * @property serialNumber The serial number of the device. Defaults to "null".
 * @property isExist A computed property that indicates whether the device exists in the system based on the presence of an ID. Returns `true` if `id` is not an empty string, `false` otherwise.
 */
data class Device(
    var id: String = "null",
    var location: String = "null",
    var locationCode: String = "null",
    var token: String = "null",
    var serialNumber: String = "null"
) {
    val isExist: Boolean
        get() = id != ""
}