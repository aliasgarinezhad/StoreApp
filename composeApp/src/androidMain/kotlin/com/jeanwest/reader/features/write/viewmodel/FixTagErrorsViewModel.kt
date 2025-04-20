package com.jeanwest.reader.features.write.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.Tag
import com.jeanwest.reader.models.TagStatus
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.EncodingType
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.epcDecoder
import com.jeanwest.reader.useCases.epcGenerator
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import com.jeanwest.reader.features.shared.NotificationPopupHost
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FixTagErrorsViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    val tags = mutableStateListOf<Tag>()
    var popupState = NotificationPopupHost()

    lateinit var barcode: Barcode
    lateinit var rf: RFID

    private val tag = "WriteViewModel"

    var product = Product()
        private set

    private var counterMaxValue = 0L
    private var counterMinValue = 0L
    private var counterValue = 0L

    var loading by mutableStateOf(false)
        private set

    fun init() {

        rf = RFID(context, state) {
            scanTrigger()
        }

        barcode = Barcode(context) {

            getItemDetails(it)
            rf.stopScanning()
            tags.clear()
            rf.epcs.clear()
            rf.tagInfos.clear()
            rf.tids.clear()
        }

        loadMemory()
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()

        rf.setTIDMode()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        if (rf.scanning) {
            rf.stopScan()
        }
        rf.setNonTIDMode()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }

        rf.stopScanning()
    }

    private fun getItemDetails(barcode: String) {

        loading = true
        api.getItemDetails(
            mutableListOf(),
            mutableListOf(barcode),
            { _, barcodes, _, _ ->

                if (barcodes.size != 1) {
                    loading = false
                    errorBeep(state)
                } else {
                    successBeep(state)
                    product = barcodes[0]
                }
                loading = false

            }, {
                loading = false
                errorBeep(state)
            })
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(context)

        counterValue = memory.getLong("value", -1L)
        counterMaxValue = memory.getLong("max", -1L)
        counterMinValue = memory.getLong("min", -1L)

    }

    private fun saveMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(context)
        val memoryEditor = memory.edit()
        memoryEditor.putLong("value", counterValue)
        memoryEditor.apply()
    }

    fun scanTrigger() {

        if (!loading) {
            barcode.startBarcodeScan()
        }
    }

    fun startBarcodeScanByButton() {
        if (!loading) {
            barcode.startChainwayScanner()
        }
    }

    fun write(tid: String) {

        CoroutineScope(IO).launch {

            loading = true

            val shouldResumeScanAfterWrite = rf.scanning.also {
                if (it) {
                    rf.stopScanning()
                }
            }

            val itemNumber = product.rfidKey
            val serialNumber = counterValue

            val productEPC = epcGenerator(
                itemNumber,
                serialNumber
            )

            counterValue++
            saveMemory()

            rf.scanningPower = 30

            if (!rf.writeWithTIDFilter(productEPC, tid)) {
                errorBeep(state)
            } else if (!rf.verify(productEPC)) {
                errorBeep(state)
            } else {
                successBeep(state)
            }

            val editedTagInfo = tags.find {
                it.tagInfo.tid == tid
            }
            tags.remove(editedTagInfo)
            rf.epcs.remove(editedTagInfo?.tagInfo?.epc)
            rf.tagInfos.remove(editedTagInfo?.tagInfo)
            rf.tids.remove(editedTagInfo?.tagInfo?.tid)

            if (shouldResumeScanAfterWrite) {
                scanAndCheckNearbyTags()
            }

            val log = """
            tag error report    
            user: ${memory.user.username}
            serialNumber: ${memory.device.serialNumber}
            old epc: ${editedTagInfo?.tagInfo?.epc}
            new epc: $productEPC
    
            """.trimIndent()
            Sentry.captureMessage(log, SentryLevel.INFO)

            loading = false
        }
    }

    private fun stopScanning() {
        loading = true
        rf.stopScanning()
        loading = false
    }

    fun onBottomBarButtonClick() {
        if (rf.scanning) {
            stopScanning()
        } else {
            scanAndCheckNearbyTags()
        }
    }

    private fun scanAndCheckNearbyTags() {

        loading = true

        CoroutineScope(IO).launch {

            rf.scanningPower = 5

            loading = false

            rf.findNearbyTagsWithResult { tagInfo ->

                val epcDetails = epcDecoder(tagInfo.epc)
                val alreadyScannedEpcs = mutableListOf<String>()

                rf.tagInfos.forEach {
                    alreadyScannedEpcs.add(it.epc)
                }

                val status = when {

                    epcDetails == null -> {
                        errorBeep(state)
                        TagStatus.RAW
                    }

                    alreadyScannedEpcs.count { tagInfo.epc == it } > 1 -> {
                        errorBeep(state)
                        TagStatus.DUPLICATE
                    }

                    epcDetails.encodingType == EncodingType.AVAKATAN -> {
                        if ((epcDetails.company == 100 && epcDetails.item == product.primaryKey) ||
                            (epcDetails.company == 101 && epcDetails.item == product.rfidKey)
                        ) {
                            successBeep(state)
                            TagStatus.CORRECT
                        } else {
                            errorBeep(state)
                            TagStatus.WRONG_DATA
                        }
                    }

                    epcDetails.encodingType == EncodingType.JOOTIJEANS -> {
                        if (epcDetails.styleCode == product.productCode &&
                            epcDetails.color == product.color &&
                            epcDetails.size == product.size
                        ) {
                            successBeep(state)
                            TagStatus.CORRECT
                        } else {
                            errorBeep(state)
                            TagStatus.WRONG_DATA
                        }
                    }

                    epcDetails.encodingType == EncodingType.primaryLight -> {
                        if (epcDetails.searchCode == product.scannedBarcode) {
                            successBeep(state)
                            TagStatus.CORRECT
                        } else {
                            errorBeep(state)
                            TagStatus.WRONG_DATA
                        }
                    }

                    else -> {
                        successBeep(state)
                        TagStatus.CORRECT
                    }
                }

                tags.add(
                    Tag(
                        tagInfo = tagInfo,
                        epcDetails = epcDetails,
                        status = status
                    )
                )
            }
        }
    }
}