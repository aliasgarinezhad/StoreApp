package com.jeanwest.reader.data.remote

import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.models.User
import org.json.JSONArray

interface Requests {

    fun getItemDetailsAndInventory(
        epcs: List<String> = mutableListOf(),
        barcodes: List<String> = mutableListOf(),
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
        local: Boolean = false,
    )

    fun getItemDetails(
        epcs: List<String> = mutableListOf(),
        barcodes: List<String> = mutableListOf(),
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
    )

    fun getStockDraftDetails(
        code: String,
        onSuccess: (draftProperties: StockDraft) -> Unit,
        onError: () -> Unit,
    )

    fun userLogin(
        username: String,
        password: String,
        locationCode: Int = 0,
        onSuccess: (user: User) -> Unit,
        onError: () -> Unit,
    )

    fun createStockDraftRequest(
        user: Int,
        source: Int,
        destination: Int,
        stockDraftRequestType: Int,
        products: List<Product> = listOf(),
        onSuccess: (stockDraftID: String) -> Unit,
        onError: () -> Unit,
    )
}