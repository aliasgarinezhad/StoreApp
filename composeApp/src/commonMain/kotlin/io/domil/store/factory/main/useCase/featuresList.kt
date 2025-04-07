package io.domil.store.factory.main.useCase

import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesScreen
import io.domil.store.factory.stopActivityFeature.view.StopActivityScreen
import io.domil.store.factory.main.model.Feature
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.add_ask
import storeapp.composeapp.generated.resources.stop

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