package com.jeanwest.reader.data

import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.Product
import org.json.JSONArray

/**
 * Interface defining the data operations for the application.
 * This interface abstracts the data source (e.g., local database, remote server)
 * from the rest of the application, providing a consistent way to access and manipulate data.
 */
interface Repository {
    fun getBarcodeDetails(
        barcode: String,
        onSuccess: (products: Product) -> Unit,
        onError: () -> Unit,
    )

    fun getItemDetailsAndInventory(
        epcs: List<String>,
        barcodes: List<String>,
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
        local: Boolean = false,
    )

    fun getCartonProducts(
        cartonCode: String,
        onSuccess: (carton: Carton) -> Unit,
        onError: () -> Unit,
    )

    fun createShelfInRequest(
        products: List<Product>,
        onSuccess: (stockDraftNumber: String, requestNumber: String) -> Unit,
        onError: (uiText: String) -> Unit
    )

}