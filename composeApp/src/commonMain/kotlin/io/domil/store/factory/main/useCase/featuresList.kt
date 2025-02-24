package io.domil.store.factory.main.useCase

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import io.domil.store.factory.main.model.Feature
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesList
import io.domil.store.factory.main.view.ReportScreen


val features = listOf(
    Feature(accessKey = "addTask", title = "ثبت فعالیت", routeScreen = ShowProductionLinesList, icon = Icons.Filled.Add),
    Feature(accessKey = "taskReports", title = "گزارش فعالیت های ثبت شده", routeScreen = ReportScreen, icon = Icons.AutoMirrored.Filled.List)
)