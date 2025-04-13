package com.jeanwest.reader.data.local

/**
 * Represents the possible locations where a feature can be active or enabled.
 *
 *  - `STORE`: The feature is active in store location.
 *  - `STORE_WAREHOUSE`: The feature is active in store and warehouse locations.
 *  - `CENTRAL_WAREHOUSE`: The feature is active in warehouse location.
 */
enum class FeatureLocation {
    STORE, STORE_WAREHOUSE, CENTRAL_WAREHOUSE
}