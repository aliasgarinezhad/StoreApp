package com.jeanwest.reader.factory.main.useCase

import com.jeanwest.reader.FeatureLocation
import com.jeanwest.reader.FeaturePlatforms
import com.jeanwest.reader.factory.addTaskFeature.view.ShowProductionLinesScreen
import com.jeanwest.reader.factory.stopActivityFeature.view.StopActivityScreen
import com.jeanwest.reader.factory.main.model.Feature
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.add_ask
import storeapp.composeapp.generated.resources.add_task
import storeapp.composeapp.generated.resources.stop

/**
 * A list of features available in the application.
 * Each feature is represented by a [Feature] object, containing:
 * - [accessKey]: A unique key to identify the feature.  This is likely used for permission checking or feature flags.
 * - [title]: The user-facing title of the feature.
 * - [routeScreen]: The screen/destination associated with the feature.  This is likely a routing mechanism to navigate the user to the correct part of the app.
 * - [iconRes]:  The resource ID of the icon representing the feature.
 */
val features = listOf(
    Feature(
        accessKey = "Action",
        title = Res.string.stop,
        routeScreen = ShowProductionLinesScreen,
        iconRes = Res.drawable.add_ask,
        locationsArray = listOf(FeatureLocation.FACTORY),
        activityClass = null,
        isNavAble = true,
        platformArray = listOf(FeaturePlatforms.WEB)
    ),
    Feature(
        accessKey = "Stop",
        title = Res.string.add_task,
        routeScreen = StopActivityScreen,
        iconRes = Res.drawable.stop,
        locationsArray = listOf(FeatureLocation.FACTORY),
        activityClass = null,
        isNavAble = true,
        platformArray = listOf(FeaturePlatforms.WEB)
    )
)