package com.jeanwest.reader.features.main.mainPage.view

import kotlin.reflect.KClass


sealed class NavigationEvents {
    data class OpenActivity(val activity: KClass<*>, val data: String?): NavigationEvents()
    data class OpenService(val service: Class<*>): NavigationEvents()
}