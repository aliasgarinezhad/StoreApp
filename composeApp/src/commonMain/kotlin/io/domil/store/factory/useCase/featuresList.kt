package io.domil.store.factory.useCase

import io.domil.store.factory.model.Feature
import io.domil.store.factory.view.AddTaskScreen
import io.domil.store.factory.view.ReportScreen


val features = listOf(
    Feature(accessKey = "addTask", title = "ثبت فعالیت", AddTaskScreen),
    Feature(accessKey = "taskReports", title = "گزارش فعالیت های ثبت شده", ReportScreen)
)