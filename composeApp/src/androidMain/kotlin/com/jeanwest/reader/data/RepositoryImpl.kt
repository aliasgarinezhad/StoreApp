package com.jeanwest.reader.data

import android.content.Context
import android.util.Log
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.CartonItem
import com.jeanwest.reader.models.Product
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject

/*
* this class is responsible for
* deciding when to call api or
* local database. it implements
* Requests interface, ensure that every
* same server functions that implemented
* in both LocalStoreDatabase and api are handled.
 */

/**
 * Implementation of the [Repository] interface.  Handles data operations,
 * interacting with both local and remote data sources based on user role and
 * available data.
 *
 * @property context Application context for accessing resources.
 * @property localStoreDatabase Local database for storing and retrieving data, primarily for store users.
 * @property api API interface for interacting with the remote server.
 * @property memory SharedPreference instance for accessing user-related data like role and warehouse information.
 */
class RepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    val localStoreDatabase: LocalStoreDatabase,
    val api: API,
    var memory: SharedPreference,
) : Repository {

    private var tag = "repository"

    override fun getBarcodeDetails(
        barcode: String,
        onSuccess: (products: Product) -> Unit,
        onError: () -> Unit,
    ) {
        getItemDetailsAndInventory(
            epcs = listOf(),
            barcodes = listOf(barcode),
            { _, barcodesDetails, _, _ ->
                if (barcodesDetails.isNotEmpty()) {
                    onSuccess(barcodesDetails[0])
                } else {
                    onError()
                }
            },
            {
                onError()
            },
        )
    }

    override fun getItemDetailsAndInventory(
        epcs: List<String>,
        barcodes: List<String>,
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
        local: Boolean,
    ) {

        if (memory.user.isStoreUser) {

            if (epcs.isEmpty() && barcodes.size == 1) {

                Log.i(
                    tag,
                    "getItemDetailsAndInventory: store use with $barcodes with local database call"
                )
                localStoreDatabase.getItemDetailsAndInventory(
                    epcs = epcs,
                    barcodes = barcodes,
                    { epcsDetails, barcodesDetails, invalidEPCs, invalidBarcodes ->
                        onSuccess(epcsDetails, barcodesDetails, invalidEPCs, invalidBarcodes)
                    },
                    {
                        onError()
                    }, true
                )
            } else {
                Log.i(
                    tag,
                    "getItemDetailsAndInventory: store use with $barcodes and $epcs with api call"
                )

                api.getItemDetailsAndInventory(
                    epcs = epcs,
                    barcodes = barcodes,
                    { epcsDetails, barcodesDetails, invalidEPCs, invalidBarcodes ->
                        onSuccess(epcsDetails, barcodesDetails, invalidEPCs, invalidBarcodes)
                    },
                    {
                        onError()
                    }, true
                )
            }
        } else {

            Log.i(
                tag,
                "getItemDetailsAndInventory: warehouse use with $barcodes and $epcs with api call"
            )
            api.getProductsV5(
                warehouses = mutableListOf(memory.user.warehouseCode.toString()),
                epcs = epcs,
                barcodes = barcodes,
                { epcsDetails, barcodesDetails, invalidEPCs, invalidBarcodes ->
                    onSuccess(epcsDetails, barcodesDetails, invalidEPCs, invalidBarcodes)
                },
                {
                    onError()
                }
            )
        }
    }

    override fun getCartonProducts(
        cartonCode: String,
        onSuccess: (carton: Carton) -> Unit,
        onError: () -> Unit,
    ) {

        api.getCartonsDetails(codes = listOf(cartonCode), onSuccess = {
            if (it.isNotEmpty()) {
                onSuccess(it[0])
            } else {
                onError()
            }
        }, onError = onError)
    }

    override fun createShelfInRequest(
        products: List<Product>,
        onSuccess: (stockDraftNumber: String, requestNumber: String) -> Unit,
        onError: (uiText: String) -> Unit,
    ) {

        Log.e(tag, products.toList().toString())
        api.createStockDraft(
            products = products,
            source = memory.user.warehouseCode,
            destination = 44,
            desc = "حواله به فروشگاه مرکزی جهت انتقال به قفسه",
            onSuccess = { stockDraftNumber ->
                Log.e(tag, products.toList().toString())

                api.createStockDraftRequest(
                    user = memory.user.username,
                    source = memory.user.warehouseCode,
                    destination = 1919,
                    stockDraftRequestType = 11,
                    products = products,
                    onSuccess = { requestNumber ->
                        onSuccess(stockDraftNumber.toString(), requestNumber)
                    }, onError = { onError("مشکلی در ایجاد درخواست به وجود آمده است.") }
                )
            },
            onError = { onError("مشکلی در ایجاد حواله به وجود آمده است.") }
        )
    }

    fun createShelfInRequestByCartons(
        cartons: List<Carton>,
        createRequestByRFID: Boolean,
        onSuccess: (stockDraftNumber: String, requestNumber: String) -> Unit,
        onError: (uiText: String) -> Unit,
    ) {

        val cartonsProducts = mutableListOf<CartonItem>()
        cartons.forEach { carton ->
            cartonsProducts.addAll(carton.products)
        }

        val stockDraftRequestProducts = mutableListOf<Product>()
        cartonsProducts.forEach {
            stockDraftRequestProducts.add(
                if (it.epcs.isEmpty() || !createRequestByRFID) {
                    it.product.copy(scannedBarcodeNumber = it.qtyInCarton)
                } else {
                    it.product.copy(scannedEPCs = it.epcs.toMutableList())
                })
        }

        api.createStockDraftByCarton(
            products = cartons,
            source = memory.user.warehouseCode,
            destination = 44,
            desc = "حواله به فروشگاه مرکزی جهت انتقال به قفسه",
            driver = 0,
            onSuccess = { stockDraftNumber ->
                api.createStockDraftRequest(
                    user = memory.user.username,
                    source = memory.user.warehouseCode,
                    destination = 1919,
                    stockDraftRequestType = 12,
                    products = stockDraftRequestProducts,
                    onSuccess = { requestNumber ->
                        onSuccess(stockDraftNumber, requestNumber)
                    }, onError = { onError("مشکلی در ایجاد درخواست به وجود آمده است.") }
                )
            },
            onError = { onError("مشکلی در ایجاد حواله به وجود آمده است.") }
        )
    }
}