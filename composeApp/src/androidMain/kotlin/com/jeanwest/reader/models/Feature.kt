package com.jeanwest.reader.models

import com.jeanwest.reader.data.local.FeatureLocation


/**
 * Represents a feature within the application.
 *
 * @property accessKey A unique identifier for the feature, used for access control or routing.
 * @property featureLocationsArray A list of [FeatureLocation] objects defining where this feature is accessible within the application's UI.  For example, this might include different tabs or menu locations.
 * @property featureTitleResourceAddress The resource ID of the string resource that holds the localized title of the feature.  This is used to display the feature's name to the user.
 * @property featureIconResourceAddress The resource ID of the drawable resource representing the icon for this feature.  This is used for visual representation of the feature in the UI.  Can be modified after initialization.
 * @property featureClass The [Class] object representing the entry point or main activity/fragment/destination associated with this feature.  This is used for launching or navigating to the feature's functionality.
 */
data class Feature(
    val accessKey: String,
    val featureLocationsArray: List<FeatureLocation>,
    val featureTitleResourceAddress: Int,
    var featureIconResourceAddress: Int,
    val featureClass: Class<*>,
)