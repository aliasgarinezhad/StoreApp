package com.jeanwest.reader

import androidx.compose.runtime.Composable
import com.jeanwest.reader.factory.addTaskFeature.data.FactoryUser
import com.jeanwest.reader.shop.data.StoreUser

actual fun saveERPUserData(storeUser: StoreUser) {
}

actual fun saveFactoryUserData(factoryUser: FactoryUser) {
}

actual fun getERPUserData(): StoreUser {
    return StoreUser()
}

actual fun getFactoryUserData(): FactoryUser {
    return FactoryUser()
}

@Composable
actual fun barcodeScanner() {
}