package com.jeanwest.reader.login.view

sealed class NavigationEvents {
    data object OpenStoreModuleEvent: NavigationEvents()
    data object OpenFactoryModuleEvent: NavigationEvents()
}