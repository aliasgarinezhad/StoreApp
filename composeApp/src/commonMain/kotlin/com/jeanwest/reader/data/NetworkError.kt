package com.jeanwest.reader.data

/**
 * Represents various network-related errors that can occur during API communication.
 *
 * Each enum value corresponds to a specific type of network issue, providing a standardized way to handle and categorize errors.
 */
enum class NetworkError : Error {
    REQUEST_TIMEOUT,
    UNAUTHORIZED,
    CONFLICT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN;
}