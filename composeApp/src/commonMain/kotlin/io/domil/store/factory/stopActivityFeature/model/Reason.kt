package io.domil.store.factory.stopActivityFeature.model

/**
 * Represents a reason with an ID and descriptive text.
 *
 * @property id The unique identifier for the reason.
 * @property text The human-readable text describing the reason.
 */
data class Reason(val id: Int, val text: String)
