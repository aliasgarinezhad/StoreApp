package com.jeanwest.reader.useCases

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.rscja.barcode.BarcodeUtility
import device.common.ScanConst
import device.sdk.ScanManager
import io.sentry.Sentry
import io.sentry.SentryLevel

class Barcode(private var context: Context, var getBarcode: (barcode: String) -> Unit = {}) {
    private var chainwayModule: BarcodeUtility? = null
    private var barcodeDataReceiver: BarcodeDataReceiver? = null
    var isEnabled = true
    var isConnectedToContext = false
    private var pointMobileModule: ScanManager = ScanManager()
    var scannedBarcodes = mutableStateListOf<String>()
    var allScannedBarcodes = mutableStateListOf<String>()
    var barcode by mutableStateOf("")
    private var scannerType = "Chainway"

    private val memory = SharedPreference(context)

    init {
        chainwayModule = BarcodeUtility.getInstance()

        when (Build.MODEL) {
            context.getString(R.string.EXARK) -> {
                scannerType = "Chainway"
            }

            context.getString(R.string.chainway) -> {
                scannerType = "Chainway"
            }

            context.getString(R.string.chainwayCapital) -> {
                scannerType = "Chainway"
            }

            context.getString(R.string.new_chainway) -> {
                scannerType = "Chainway"
            }

            context.getString(R.string.point_mobile) -> {
                scannerType = "PM85"
            }

            context.getString(R.string.point_mobile2) -> {
                scannerType = "PM85"
            }

            else -> {
                scannerType = "test"
            }
        }

        enable()
    }

    fun connectWithContext() {

        if (!isConnectedToContext) {
            if (barcodeDataReceiver == null) {
                barcodeDataReceiver = BarcodeDataReceiver()
                val intentFilter = IntentFilter()
                intentFilter.addAction("com.scanner.broadcast")
                intentFilter.addAction(ScanConst.INTENT_EVENT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    context.registerReceiver(
                        barcodeDataReceiver, intentFilter,
                        Context.RECEIVER_NOT_EXPORTED
                    )
                } else {
                    registerReceiverOnOlderDevices()
                }
            }

            if (scannerType == "PM85") {
                pointMobileModule.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_EVENT)
                pointMobileModule.aDecodeSetTerminator(ScanConst.Terminator.DCD_TERMINATOR_NONE)
            } else if (scannerType == "Chainway") {
                chainwayModule!!.setOutputMode(context, 2)
                chainwayModule!!.enableEnter(context, false)
            }
            isConnectedToContext = true
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")

    private fun registerReceiverOnOlderDevices() {
        barcodeDataReceiver = BarcodeDataReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.scanner.broadcast")
        intentFilter.addAction(ScanConst.INTENT_EVENT)
        context.registerReceiver(barcodeDataReceiver, intentFilter)
    }

    fun startBarcodeScan() {
        if (scannerType == "PM85" && isConnectedToContext) {
            pointMobileModule.aDecodeSetTriggerOn(1)
        }
    }

    fun startChainwayScanner() {
        if(scannerType == "Chainway" && isConnectedToContext) {
            chainwayModule!!.startScan(context, BarcodeUtility.ModuleType.BARCODE_2D)
        }
    }

    fun enable() {
        if (!isEnabled) {
            if (scannerType == "PM85") {
                val scannerEnableIntent = Intent("device.common.ENABLED_SCANNER")
                scannerEnableIntent.putExtra("EXTRA_ENABLED_SCANNER", 1)
                context.sendBroadcast(scannerEnableIntent)
            } else if (scannerType == "Chainway") {
                if (chainwayModule != null) {
                    chainwayModule!!.open(context, BarcodeUtility.ModuleType.BARCODE_2D)
                }
            }
            isEnabled = true
        }
    }

    fun disable() {
        if (isEnabled) {
            if (scannerType == "PM85") {
                val scannerDisableIntent = Intent("device.common.ENABLED_SCANNER")
                scannerDisableIntent.putExtra("EXTRA_ENABLED_SCANNER", 0)
                context.sendBroadcast(scannerDisableIntent)
            } else if (scannerType == "Chainway") {
                if (chainwayModule != null) {
                    chainwayModule!!.close(context, BarcodeUtility.ModuleType.BARCODE_2D)
                }
            }
            isEnabled = false
        }
    }

    fun disconnectFromContext() {

        if (isConnectedToContext) {
            if (barcodeDataReceiver != null) {
                context.unregisterReceiver(barcodeDataReceiver)
                barcodeDataReceiver = null
            }

            if (scannerType == "PM85") {
                pointMobileModule.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_CTRLV)
                pointMobileModule.aDecodeSetTerminator(ScanConst.Terminator.DCD_TERMINATOR_LF)
            } else if (scannerType == "Chainway") {
                if (chainwayModule != null) {
                    chainwayModule!!.enableEnter(context, true)
                    chainwayModule!!.setOutputMode(context, 3)
                }
            }
            isConnectedToContext = false
        }

        if (allScannedBarcodes.size > 0) {
            val clearedBarcodes = allScannedBarcodes.filter {
                it !in scannedBarcodes
            }.toList()

            Sentry.captureMessage(
                """
                cleared barcodes list
                user: ${memory.user.username}
                serialNumber: ${memory.device.serialNumber}
                qty: ${clearedBarcodes.size}
                $clearedBarcodes
            """.trimIndent(), SentryLevel.INFO
            )
        }
    }

    inner class BarcodeDataReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (scannerType == "PM85") {

                val status = intent.getBooleanExtra(ScanConst.EXTRA_EVENT_DECODE_RESULT, false)
                val decodeBytesLength = intent.getIntExtra(ScanConst.EXTRA_EVENT_DECODE_LENGTH, 0)
                val decodeBytesValue = intent.getByteArrayExtra(ScanConst.EXTRA_EVENT_DECODE_VALUE)
                val barcode = String(decodeBytesValue!!, 0, decodeBytesLength)

                if (status) {

                    if (barcode.isNotEmpty()) {
                        this@Barcode.barcode = barcode
                        scannedBarcodes.add(this@Barcode.barcode)
                        allScannedBarcodes.add(this@Barcode.barcode)
                        getBarcode(this@Barcode.barcode)
                    }
                }
            } else if (scannerType == "Chainway") {
                val barcode = intent.getStringExtra("data")
                val status = intent.getStringExtra("SCAN_STATE")
                if (status != null && status == "cancel") {
                    return
                } else {
                    if (barcode == null) {
                        return
                    }
                    if (barcode.isNotEmpty()) {
                        this@Barcode.barcode = barcode
                        scannedBarcodes.add(this@Barcode.barcode)
                        allScannedBarcodes.add(this@Barcode.barcode)
                        getBarcode(this@Barcode.barcode)
                    }
                }
            }
        }
    }
}