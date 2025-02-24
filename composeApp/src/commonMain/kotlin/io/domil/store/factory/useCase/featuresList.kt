package io.domil.store.factory.useCase

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import io.domil.store.factory.model.Feature
import io.domil.store.factory.view.AddTaskScreen
import io.domil.store.factory.view.ReportScreen


val features = listOf(
    Feature(accessKey = "addTask", title = "ثبت فعالیت", routeScreen = AddTaskScreen, icon = Icons.Filled.Add),
    Feature(accessKey = "taskReports", title = "گزارش فعالیت های ثبت شده", routeScreen = ReportScreen, icon = Icons.AutoMirrored.Filled.List)
)