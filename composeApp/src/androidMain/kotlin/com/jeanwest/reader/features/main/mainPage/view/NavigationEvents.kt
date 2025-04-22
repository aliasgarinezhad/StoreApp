package com.jeanwest.reader.features.main.mainPage.view


sealed class NavigationEvents {
    data class OpenActivity(val activity: Class<*>, val data: String?): NavigationEvents()
    data class OpenService(val service: Class<*>): NavigationEvents()
}