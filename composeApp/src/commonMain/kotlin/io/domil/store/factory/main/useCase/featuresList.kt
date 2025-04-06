package io.domil.store.factory.main.useCase

import io.domil.store.factory.main.model.Feature
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesScreen
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.add_ask


val features = listOf(
    Feature(accessKey = "Action", title = "ثبت فعالیت", routeScreen = ShowProductionLinesScreen, iconRes = Res.drawable.add_ask),
    //Feature(accessKey = "taskReports", title = "گزارش فعالیت های ثبت شده", routeScreen = ReportScreen, icon = Icons.AutoMirrored.Filled.List)
)