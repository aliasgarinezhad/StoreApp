package com.jeanwest.reader.features.stockDraft.view

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.jalaliDate.JalaliDateConverter
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.AndroidEntryPoint
import ir.huri.jcal.JalaliCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftsHistory : ComponentActivity() {

    private lateinit var barcode: Barcode
    var scanningMode by mutableStateOf(false)

    @Inject
    lateinit var viewModel: StockDraftsHistoryViewModel

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        exceptionHandler()
    }

    private fun init() {
        barcode = Barcode(this)

        viewModel.warehouseCodeToString = { code ->
            convertWarehouseCodeToText(code)
        }

        viewModel.convertToISO8601 = { day, month, year ->
            jalaliToISO8601(year, month, day)
        }

        viewModel.callApi = {
            checkParamsAndCallApi()
        }
        viewModel.uiListOnClick = { product ->
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("stockDraftId", product.stockDraftID)
            clipboard.setPrimaryClip(clip)
            showLog("شماره حواله کپی شد.", viewModel.state)
        }
        viewModel.back = { back() }
        viewModel.dateConverter = {
            dateConvertor(it)
        }

        //init source dropDown menu
        viewModel.allSourceWarehouseFilterValues.clear()
        viewModel.allSourceWarehouseFilterValues.add("همه مبدا ها")
        viewModel.allSourceWarehouseFilterValues.addAll(memory.user.destinationTitles)
        viewModel.sourceFilterOnClick = {

            viewModel.sourceWarehouseFilterValue = it
            viewModel.source =
                memory.user.destinationMapWithId[viewModel.sourceWarehouseFilterValue]
            if (viewModel.destinationWarehouseFilterValue != "همه مقصد ها") {
                viewModel.des =
                    memory.user.destinationMapWithId[viewModel.destinationWarehouseFilterValue]
            } else {
                viewModel.des = null
            }
            checkParamsAndCallApi()
        }

        //init destination dropDown menu
        viewModel.allDestinationWarehouseFilterValues.clear()
        viewModel.allDestinationWarehouseFilterValues.add("همه مقصد ها")
        viewModel.allDestinationWarehouseFilterValues.addAll(memory.user.destinationTitles)
        viewModel.destinationFilterOnClick = {
            viewModel.destinationWarehouseFilterValue = it
            viewModel.des =
                memory.user.destinationMapWithId[viewModel.destinationWarehouseFilterValue]
            if (viewModel.sourceWarehouseFilterValue != "همه مبدا ها") {
                viewModel.source =
                    memory.user.destinationMapWithId[viewModel.sourceWarehouseFilterValue]
            } else {
                viewModel.source = null
            }
            checkParamsAndCallApi()

        }

        //init state dropDown menu
        viewModel.stateFilterOnClick = {
            viewModel.stockDraftStateFilterValue = it
            if (viewModel.sourceWarehouseFilterValue != "همه مبدا ها") {
                viewModel.source =
                    memory.user.destinationMapWithId[viewModel.sourceWarehouseFilterValue]
            } else {
                viewModel.source = null
            }

            if (viewModel.destinationWarehouseFilterValue != "همه مقصد ها") {
                viewModel.des =
                    memory.user.destinationMapWithId[viewModel.destinationWarehouseFilterValue]
            } else {
                viewModel.des = null
            }
            checkParamsAndCallApi()
        }

        checkParamsAndCallApi()
    }

    private fun checkParamsAndCallApi() {
        var pended = false
        if (memory.user.warehouses.keys.contains(viewModel.source.toString()) || memory.user.warehouses.keys.contains(
                viewModel.des.toString()
            )
        ) {
            when (viewModel.stockDraftStateFilterValue) {
                "همه" -> {
                    viewModel.stateId = null
                }

                "نهایی" -> {
                    viewModel.stateId = 2
                }

                "ابطال شده" -> {
                    viewModel.stateId = 9
                }

                "حواله های باز" -> {
                    viewModel.uiList.clear()
                    pended = true
                }
            }
            getStockDraftsDetails(
                viewModel.source,
                viewModel.des,
                viewModel.stDate,
                viewModel.endDate,
                viewModel.stateId,
                pended
            )
        } else {
            viewModel.uiList.clear()
            showLog(
                "حداقل یکی از مبدا یا مقصد انتخابی باید فروشگاه خودتان باشد.",
                viewModel.state
            )
        }
    }

    private fun jalaliToISO8601(jalaliYear: Int, jalaliMonth: Int, jalaliDay: Int): String {
        // Convert Jalali date to Gregorian date
        val jalaliCalendar = JalaliCalendar(jalaliYear, jalaliMonth, jalaliDay)
        val gregorianDate = jalaliCalendar.toGregorian()

        // Ensure the GregorianCalendar is set to midnight UTC
        gregorianDate.timeZone = TimeZone.getTimeZone("UTC")
        gregorianDate.set(Calendar.HOUR_OF_DAY, 0)
        gregorianDate.set(Calendar.MINUTE, 0)
        gregorianDate.set(Calendar.SECOND, 0)
        gregorianDate.set(Calendar.MILLISECOND, 0)

        // Define the output format (ISO 8601)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Return the formatted date string
        return isoFormat.format(gregorianDate.time)
    }


    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    private fun convertWarehouseCodeToText(code: String): String {
        return memory.erpData.warehousesIDsToTitles[code].toString()
    }

    override fun onPause() {
        super.onPause()
        viewModel.state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.state.currentSnackbarData?.dismiss()
        if (barcode.isEnabled) {
            barcode.disable()
        }
        setContent {
            Page(viewModel)
        }
    }

    private fun getStockDraftsDetails(
        source: Int?,
        des: Int?,
        stDate: String?,
        endDate: String?,
        stateId: Int?,
        showPended: Boolean?,
    ) {

        viewModel.loading = true
        api.stockDraftsHistory(source, des, stDate, endDate, stateId, showPended, perPage = "80" ,{ it ->
            viewModel.loading = false
            viewModel.uiList.clear()
            viewModel.uiList.addAll(it)
            viewModel.uiList.sortedByDescending {
                it.createDate
            }
        }, {
            viewModel.loading = false
        })
    }

    private fun dateConvertor(date: String): String {
        val intArrayFormatJalaliCreateDate = JalaliDateConverter.gregorian_to_jalali(
            date.substring(0, 4).toInt(),
            date.substring(5, 7).toInt(),
            date.substring(8, 10).toInt()
        )
        return "${intArrayFormatJalaliCreateDate[0]}/${intArrayFormatJalaliCreateDate[1]}/${intArrayFormatJalaliCreateDate[2]}"
    }

    private fun back() {
        finish()
    }
}
