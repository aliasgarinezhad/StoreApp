package com.jeanwest.reader.features.stockDraft.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.SnackbarHostState
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import com.jeanwest.reader.features.shared.SnackBarActions
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class CreateStockDraftFromCarton : ComponentActivity() {

    lateinit var barcode: Barcode
    lateinit var rf: RFID

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var api: API

    @Inject
    lateinit var viewModel: CreateStockDraftFromCartonView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContent {
            Page(viewModel)
        }
    }

    private fun init() {
        loadPrintersList()
        exceptionHandler()
        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this) {
            getCartonDetails(it)
        }
        viewModel.back = { back() }
        viewModel.clearItem = { clear(it) }
        viewModel.clear = { clear() }
        viewModel.enterBarcodeOnClick = {
            getCartonDetails(it)
        }
        viewModel.createSack = { createSack() }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    override fun onPause() {
        super.onPause()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
    }

    override fun onResume() {
        super.onResume()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
    }

    private fun loadPrintersList() {
        viewModel.loading = true
        api.getPrintersList({
            viewModel.printers.clear()
            viewModel.printers.putAll(it)
            viewModel.printer = viewModel.printers.keys.toMutableList()[0]
            viewModel.loading = false
        }, {
            viewModel.loading = false
        })
    }

    private fun getCartonDetails(stockDraft: String) {
        viewModel.loading = true
        if (stockDraft.startsWith("CN")) {
            if (stockDraft in viewModel.stockDraftIDs) {
                showLog("کارتن قبلا اسکن شده است.", state)
                viewModel.loading = false
                errorBeep(state)
            } else {
                api.getCartonsDetails(listOf(stockDraft), {
                    if (it.isNotEmpty()) {
                        if (it[0].operationSource != it[0].operationDes) {
                            viewModel.cartonUiList.add(it[0])
                            viewModel.stockDraftIDs.add(stockDraft)
                            successBeep(state)
                        } else {
                            showLog("مبدا کارتن با مقصد آن یکی است.", state)
                            errorBeep(state)
                        }

                    } else {
                        showLog("شماره کارتن نامعتبر است.", state)
                        errorBeep(state)
                    }
                    viewModel.loading = false
                }, {
                    errorBeep(state)
                    viewModel.loading = false
                })
            }
        } else {
            showLog("شماره کارتن نامعتبر است.", state)
            errorBeep(state)
            viewModel.loading = false
            viewModel.stockDraftNumber = ""
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 4) {
                viewModel.back()
            }
        }
        return true
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    private fun clear() {
        viewModel.stockDraftIDs.clear()
        viewModel.cartonUiList.clear()
        viewModel.draftsMap.clear()
        //saveToMemory()
    }

    fun clear(stockDraftID: String) {
        viewModel.stockDraftIDs.remove(stockDraftID)
        viewModel.cartonUiList.removeAll {
            it.number == stockDraftID
        }
        viewModel.draftsMap.remove(stockDraftID)
        //saveToMemory()
    }

    fun back() {
        //saveToMemory()
        finish()
    }

    private fun createSack() {
        viewModel.creatingStockDraft = true
        viewModel.loading = true

        val cartonIds = mutableListOf<Long>()
        viewModel.stockDraftIDs.forEach {
            cartonIds.add(it.substring(3).toLong())
        }


        api.convertCartonToStockDraft(
            cartonIds,
            viewModel.printers[viewModel.printer] ?: 0,
            {
                viewModel.loading = false
                viewModel.creatingStockDraft = false
                showLog(it, state, action = SnackBarActions.SUCCESS)
            },
            {
                viewModel.loading = false
                viewModel.creatingStockDraft = false
            })
    }
}