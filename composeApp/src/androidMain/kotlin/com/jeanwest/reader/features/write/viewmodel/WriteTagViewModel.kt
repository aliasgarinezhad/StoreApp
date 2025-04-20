package com.jeanwest.reader.features.write.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.doneColor
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.features.shared.warningColor
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.EncodingType
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.epcDecoder
import com.jeanwest.reader.useCases.epcGenerator
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.SnackBarActions
import com.jeanwest.reader.features.shared.doneColor
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.features.shared.warningColor

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for writing RFID tags.
 *
 * This ViewModel handles the logic for scanning barcodes, identifying nearby RFID tags,
 * writing data to the tags, and interacting with the API and local storage.  It utilizes
 * RFID and barcode scanning capabilities, manages user feedback through snackbars and
 * popups, and logs events for debugging and monitoring.
 *
 * @property state A [SnackbarHostState] for displaying snackbar messages to the user.
 * @property memory A [SharedPreference] instance for accessing shared preferences, including user and device information.
 * @property api An [API] instance for making network requests to the backend.
 * @property context The application [Context].
 */
@HiltViewModel
class WriteTagViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    var popupState = NotificationPopupHost()

    lateinit var barcode: Barcode
    lateinit var rf: RFID

    private var writeEPCFilter = ""
    private val tag = "WriteViewModel"

    private lateinit var product: Product

    private var nextButtonTriggerShouldRunWriteTagCommand = false
        set(value) {
            if (!value) {
                if (!barcode.isEnabled) {
                    barcode.enable()
                }
            } else {
                if (barcode.isEnabled) {
                    barcode.disable()
                }
            }
            field = value
        }
    var counterMaxValue = 0L
    private var counterMinValue = 0L
    var counterValue = 0L

    var resultColor by mutableStateOf(Color.White)
        private set
    var result by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set

    fun init() {

        rf = RFID(context, state) {
            scanTrigger()
        }

        barcode = Barcode(context) {
            if (!nextButtonTriggerShouldRunWriteTagCommand) {
                result = "${barcode.barcode}\n"
                resultColor = doneColor
                nextButtonTriggerShouldRunWriteTagCommand = true
            }
        }

        loadMemory()
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()

        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }

        if (counterValue >= counterMaxValue && barcode.isEnabled) {
            barcode.disable()
        }
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        nextButtonTriggerShouldRunWriteTagCommand = false
        if (rf.scanning) {
            rf.stopScan()
        }
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
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

        if (counterValue >= counterMaxValue) {
            showLog(
                "مجوز رایت وجود ندارد یا به پایان رسیده است. برای دریافت مجوز با پشتیبانی تماس بگیرید.",
                state
            )
            return
        }

        if (!loading) {
            if (nextButtonTriggerShouldRunWriteTagCommand) {
                writeTag(barcode.barcode)
            } else {
                barcode.startBarcodeScan()
            }
        }
    }

    private fun write(product: Product, tagFilter: String) {

        CoroutineScope(IO).launch {

            loading = true

            val itemNumber = product.rfidKey
            val serialNumber = counterValue

            val productEPC = epcGenerator(
                itemNumber,
                serialNumber
            )

            counterValue++
            saveMemory()

            rf.scanningPower = 30

            if (!rf.write(productEPC, tagFilter)) {
                result += "تگ رایت نشده است. لطفا دوباره امتحان کنید"
                errorBeep(state)
                resultColor = errorLight
            } else if (!rf.verify(productEPC)) {
                result += "تگ درست رایت نشده است. لطفا دوباره امتحان کنید"
                errorBeep(state)
                resultColor = errorLight
            } else {
                successBeep(state)
                sendLog(barcode.barcode, productEPC)
                result += "تگ با موفقیت رایت شد"
                resultColor = doneColor
            }
            nextButtonTriggerShouldRunWriteTagCommand = false
            loading = false
        }
    }

    private fun writeTag(barcodeID: String) {

        loading = true

        CoroutineScope(IO).launch {

            var epc = ""
            val rawEpcs = mutableListOf<String>()

            rf.scanningPower = 5

            rf.epcs.clear()
            rf.findNearbyTags()
            delay(1000)
            rf.stopScanning()

            rf.epcs.forEach {
                if (!it.startsWith("30") && !it.startsWith("000") && !it.startsWith("1")) {
                    rawEpcs.add(it)
                }
            }

            Log.e(tag, rf.epcs.toList().toString())
            Log.e(tag, rawEpcs.toList().toString())

            when {
                rf.epcs.isEmpty() -> {
                    result += "هیج تگی پیدا نشد. لطفا دستگاه را نزدیک تگ قرار دهید و دوباره تلاش کنید."
                    errorBeep(state)
                    resultColor = errorLight
                    nextButtonTriggerShouldRunWriteTagCommand = false
                    loading = false
                    return@launch
                }

                rawEpcs.size > 1 -> {
                    result += "تعداد تگ های خام پیدا شده بیشتر از یک است. لطفا تگ مورد نظرتان را جدا از بقیه قرار دهید و دوباره تلاش کنید."
                    errorBeep(state)
                    resultColor = errorLight
                    nextButtonTriggerShouldRunWriteTagCommand = false
                    loading = false
                    return@launch
                }

                rf.epcs.size > 1 && rawEpcs.isEmpty() -> {
                    result += "تعداد تگ های پیدا شده بیشتر از یک است. لطفا تگ مورد نظرتان را جدا از بقیه قرار دهید و دوباره تلاش کنید."
                    errorBeep(state)
                    resultColor = errorLight
                    nextButtonTriggerShouldRunWriteTagCommand = false
                    loading = false
                    return@launch
                }

                rf.epcs.size == 1 && rawEpcs.isEmpty() -> {
                    epc = rf.epcs[0]
                    writeEPCFilter = epc
                }

                rawEpcs.size == 1 -> {
                    epc = rawEpcs[0]
                    writeEPCFilter = epc
                }

                else -> {
                    result += "مشکلی در ارتباط با تگ به وجود آمده است."
                    errorBeep(state)
                    resultColor = errorLight
                    nextButtonTriggerShouldRunWriteTagCommand = false
                    loading = false
                    return@launch
                }
            }

            api.getItemDetails(
                mutableListOf(),
                mutableListOf(barcodeID),
                { _, barcodes, _, _ ->

                    val decodedTagEpc = epcDecoder(epc)

                    if (barcodes.size != 1) {
                        result += "بارکد مورد نظر در سیستم تعریف نشده است. لطفا با پشتیبانی تماس بگیرید."
                        errorBeep(state)
                        resultColor = errorLight
                        nextButtonTriggerShouldRunWriteTagCommand = false
                        loading = false
                    } else if (barcodes[0].rfidKey == 0L) {
                        showLog(
                            "بارکد موردنظر مشکلی در rfidCode دارد. لطفا با پشتیبانی تماس بگیرید.",
                            state,
                            SnackBarActions.ERROR
                        )
                    } else if (decodedTagEpc == null) {
                        write(barcodes[0], writeEPCFilter)
                    } else {

                        when (decodedTagEpc.encodingType) {

                            EncodingType.AVAKATAN -> {
                                if ((decodedTagEpc.company == 100 && decodedTagEpc.item == barcodes[0].primaryKey) ||
                                    (decodedTagEpc.company == 101 && decodedTagEpc.item == barcodes[0].rfidKey)
                                ) {
                                    showAlreadyWrittenWithSameEPCWarning()
                                } else {
                                    replaceTagEPC(barcodes[0], writeEPCFilter)
                                }
                            }

                            EncodingType.JOOTIJEANS -> {
                                if (decodedTagEpc.styleCode == barcodes[0].productCode &&
                                    decodedTagEpc.color == barcodes[0].color &&
                                    decodedTagEpc.size == barcodes[0].size
                                ) {

                                    showAlreadyWrittenWithSameEPCWarning()
                                } else {
                                    replaceTagEPC(barcodes[0], writeEPCFilter)
                                }
                            }

                            EncodingType.primaryLight -> {
                                Log.e(tag, decodedTagEpc.searchCode)
                                Log.e(tag, barcodeID)
                                if (decodedTagEpc.searchCode == barcodeID) {
                                    showAlreadyWrittenWithSameEPCWarning()
                                } else {
                                    replaceTagEPC(barcodes[0], writeEPCFilter)
                                }
                            }
                        }
                    }
                },
                {
                    nextButtonTriggerShouldRunWriteTagCommand = false
                    loading = false
                    errorBeep(state)
                    resultColor = errorLight
                })
        }
    }

    private fun showAlreadyWrittenWithSameEPCWarning() {
        successBeep(state)
        resultColor = warningColor
        result += "این تگ قبلا با همین بارکد رایت شده است" + "\n"
        nextButtonTriggerShouldRunWriteTagCommand = false
        loading = false
    }

    private fun replaceTagEPC(product: Product, tag: String) {
        this.product = product
        result += "این تگ قبلا با بارکد دیگری رایت شده است" + "\n"
        nextButtonTriggerShouldRunWriteTagCommand = false
        loading = false
        popupState.showPopupWith2Button(
            message = "این تگ قبلا با بارکد دیگری رایت شده است. مقدار جدید جایگزین قبلی شود؟",
            onOkClick = { write(product, tag) },
            onCancelClick = {
                nextButtonTriggerShouldRunWriteTagCommand = false
                loading = false
            }
        )
    }

    private fun sendLog(barcode: String, epc: String) {
        val additionalInfo = """
            write info:
            user: ${memory.user.username}
            serialNumber: ${memory.device.serialNumber}
            barcode: $barcode
            epc: $epc
        """.trimIndent()
        Sentry.captureMessage(additionalInfo, SentryLevel.INFO)
    }
}