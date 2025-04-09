package io.domil.store.factory.main.useCase

import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesScreen
import io.domil.store.factory.stopActivityFeature.view.StopActivityScreen
import io.domil.store.factory.main.model.Feature
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.add_ask
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
        title = "ثبت فعالیت",
        routeScreen = ShowProductionLinesScreen,
        iconRes = Res.drawable.add_ask
    ),
    Feature(
        accessKey = "Stop",
        title = "توقفات",
        routeScreen = StopActivityScreen,
        iconRes = Res.drawable.stop
    )
)