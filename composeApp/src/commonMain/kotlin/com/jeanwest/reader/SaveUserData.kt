package com.jeanwest.reader

import androidx.compose.runtime.Composable
import com.jeanwest.reader.factory.addTaskFeature.data.FactoryUser
import com.jeanwest.reader.shop.data.StoreUser

expect fun saveERPUserData(storeUser: StoreUser)
expect fun saveFactoryUserData(factoryUser: FactoryUser)
expect fun getERPUserData(): StoreUser
expect fun getFactoryUserData(): FactoryUser
@Composable expect fun barcodeScanner()
