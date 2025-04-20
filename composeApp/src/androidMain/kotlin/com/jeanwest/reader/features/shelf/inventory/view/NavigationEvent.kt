package com.jeanwest.reader.features.shelf.inventory.view

sealed class NavigationEvent {
    data object BackToEnterShelfNumberEvent : NavigationEvent()
    data object GoToShelfInventoryEvent : NavigationEvent()
}