package reader.features

import android.util.Log
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.features.stockDraft.view.StockDraftConfirm
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.testData.stockDraftProducts
import com.jeanwest.reader.testData.stockDraftProperties
import org.json.JSONArray
import org.junit.Rule
import org.junit.Test

class StockDraftConfirmTest {

    @get:Rule
    val activity = createAndroidComposeRule<StockDraftConfirm>()

    //RUN CREATE STOCK DRAFT CREATE TEST BEFORE THIS TEST
    @Test
    fun testWithBarcode() {

        waitForFinishLoading()

        //To make sure user is on main screen
        if (activity.activity.scanningMode) {
            activity.onNodeWithText("تایید نهایی").performClick()
            activity.waitForIdle()
            activity.onNodeWithTag("notConfirm").performClick()
            activity.waitForIdle()
            waitForFinishLoading()
            activity.onNodeWithText("کسری").assertDoesNotExist()
        }

        //Compare that the displayed information is equal to the original list
        for (i in 0 until activity.activity.stockDraftUiList.size) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains("حواله: " + activity.activity.stockDraftUiList[i].stockDraftID.toString())
                assertTextContains("تعداد کالاها: " + activity.activity.stockDraftUiList[i].sumProductQty)
                assertTextContains("تاریخ: " + activity.activity.stockDraftUiList[i].createDate)
                assertTextContains("از: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.stockDraftUiList[i].fromWareHouseID])
                assertTextContains("شرح: " + activity.activity.stockDraftUiList[i].stockDraftDescription)
                assertTextContains("به: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.stockDraftUiList[i].toWareHouseID.toString()])
            }
        }

        //To receive previos sended stock draft data
        val memory = PreferenceManager.getDefaultSharedPreferences(activity.activity)
        val type = object : TypeToken<MutableList<String>>() {}.type
        val stockDraftNumber: String = memory.getString("stockDraftNumber", "") ?: ""
        val barcode = Gson().fromJson(memory.getString("StockDraftBarcodeForTest", ""), type)
            ?: mutableListOf<String>()
        val totalShortage = barcode.size

        //Simulating user enter stockDraft number
        activity.onNodeWithText("شماره حواله").performTextInput(stockDraftNumber)
        activity.onNodeWithText("شماره حواله").performImeAction()
        activity.waitForIdle()
        waitForFinishLoading()

        Log.e("shortage number: ", totalShortage.toString())
        Log.e("stockDraftNumber: ", stockDraftNumber)

        //checking params
        activity.onNodeWithText("کسری: $totalShortage").assertExists()
        activity.onNodeWithText("اضافی: 0").assertExists()
        activity.onNodeWithText("اسکن: 0").assertExists()

        //Simulating scanning product by user
        if (barcode.isNotEmpty()) {
            for (i in 0 until barcode.size) {
                barcodeScan(barcode[i])
                waitForFinishLoading()
            }
        }

        //checking params
        activity.onNodeWithText("کسری: 0", true, false, true).assertExists()
        activity.onNodeWithText("اضافی: 0").assertExists()
        activity.onNodeWithText("اسکن: $totalShortage").assertExists()

        activity.onNodeWithText("تایید نهایی").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بله").performClick()
        waitForFinishLoading()
        activity.waitForIdle()
        activity.waitForIdle()
        activity.onNodeWithText("باشه").performClick()
        Thread.sleep(5000)
    }

    //test separate delete on Additional items
    @Test
    fun test2() {

        waitForFinishLoading()
        if (!activity.activity.scanningMode) {
            barcodeScan(stockDraftProperties.number.toString())
            waitForFinishLoading()
            activity.onNodeWithText("کسری").assertExists()
        }
        clearUserData()
        activity.onNodeWithTag("scanTypeDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بارکد").performClick()
        activity.waitForIdle()
        barcodeScan("91533902J-2200-XXL")
        activity.waitForIdle()
        activity.onAllNodesWithText("کسری")[0].performClick()
        activity.waitForIdle()
        activity.onAllNodesWithText("اضافی")[0].performClick()
        activity.waitForIdle()
        activity.onAllNodesWithText("اضافی: 1")[0].assertExists()

        activity.onAllNodesWithTag("clear")[0].performClick()
        activity.waitForIdle()

        activity.onNodeWithText("اضافی: 0").assertExists()

        val scannedBarcodes = activity.activity.barcode.scannedBarcodes
        val epcs = activity.activity.rf.epcs
        val scannedBarcodesMap = activity.activity.scannedBarcodeMapWithProperties
        assert(!(scannedBarcodes.isNotEmpty() && scannedBarcodes.contains(activity.activity.itemsUiList[0].KBarCode)))
        assert(!(epcs.isNotEmpty() && epcs.contains(activity.activity.itemsUiList[0].rfidKey.toString())))
        assert(!(scannedBarcodesMap.isNotEmpty() && scannedBarcodesMap.contains(activity.activity.itemsUiList[0].scannedEPCs[0])))
    }

    @Test
    fun test3() {

        waitForFinishLoading()

        if (activity.activity.scanningMode) {
            activity.onNodeWithText("تایید نهایی").performClick()
            activity.waitForIdle()
            activity.onNodeWithTag("notConfirm").performClick()
            activity.waitForIdle()
            waitForFinishLoading()
            activity.onNodeWithText("کسری").assertDoesNotExist()
        }

        val kbarcodes = mutableListOf<String>()
        val productJson = JSONArray(stockDraftProducts)

        for (i in 0 until productJson.length()) {
            kbarcodes.add(
                productJson.getJSONObject(i).getString("kbarcode")
            )
        }

        barcodeScan(stockDraftProperties.number.toString())
        waitForFinishLoading()

        activity.onNodeWithText("کسری: ${stockDraftProperties.numberOfItems}").assertExists()
        activity.onNodeWithText("اضافی: 0").assertExists()
        activity.onNodeWithText("اسکن: 0").assertExists()

        activity.activity.inputProducts.forEach {
            assert(it.value.draftNumber == kbarcodes.count { it1 ->
                it.value.KBarCode == it1
            })
        }

        val products = mutableListOf<Product>()
        products.addAll(activity.activity.inputProducts.values)
        products.sortBy {
            it.productCode
        }
        products.sortBy {
            it.name
        }

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(products[i].KBarCode)
                assertTextContains(products[i].name)
                assertTextContains("موجودی: " + products[i].draftNumber)
                assertTextContains("کسری: " + products[i].draftNumber)
            }
        }

        epcScan(activity.activity.draftProperties.epcsToPrimaryKeysMap.keys.toMutableList())
        waitForFinishLoading()

        activity.onNodeWithText("کسری: 0").assertExists()
        activity.onNodeWithText("اضافی: 0").assertExists()
        activity.onNodeWithText("اسکن: ${stockDraftProperties.numberOfItems}").assertExists()

        activity.onNodeWithText("تایید نهایی").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("confirm").performClick()
        waitForFinishLoading()
    }

    private fun checkResults(products: MutableList<Product>) {

        var additionalNumber = 0
        var scannedNumber = 0

        products.forEach {
            if (it.draftNumber >= 3) {
                activity.activity.productConflicts.forEach { it1 ->
                    if (it1.KBarCode == it.KBarCode) {
                        assert(it1.conflictNumber == 3 && it1.conflictType == "کسری")
                        scannedNumber += it1.scannedNumber
                    }
                }
            } else if (it.draftNumber == 2) {
                activity.activity.productConflicts.forEach { it1 ->
                    if (it1.KBarCode == it.KBarCode) {
                        assert(it1.conflictNumber == 3 && it1.conflictType == "اضافی")
                        additionalNumber += 3
                        scannedNumber += it1.scannedNumber
                    }
                }
            } else {
                activity.activity.productConflicts.forEach { it1 ->
                    if (it1.KBarCode == it.KBarCode) {
                        assert(it1.conflictNumber == 1 && it1.conflictType == "اضافی")
                        additionalNumber += 1
                        scannedNumber += it1.scannedNumber
                    }
                }
            }
        }

        activity.onAllNodesWithText("کسری: ${stockDraftProperties.numberOfItems - (scannedNumber - additionalNumber)}")[0].assertExists()
        activity.onNodeWithText("اضافی: $additionalNumber").assertExists()
        activity.onNodeWithText("اسکن: $scannedNumber").assertExists()

        val shortageProductList = activity.activity.productConflicts.filter {
            it.conflictType == "کسری"
        }.toMutableList()

        shortageProductList.sortBy {
            it.productCode
        }
        shortageProductList.sortBy {
            it.name
        }

        val forLoopMaxValue = if (shortageProductList.size > 3) 3 else shortageProductList.size
        for (i in 0 until forLoopMaxValue) {

            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(shortageProductList[i].KBarCode)
                assertTextContains(shortageProductList[i].name)
                assertTextContains("موجودی: " + shortageProductList[i].draftNumber)
                assertTextContains("کسری: " + shortageProductList[i].conflictNumber)
            }
        }

        val additionalProductList = activity.activity.productConflicts.filter {
            it.conflictType == "اضافی"
        }.toMutableList()

        additionalProductList.sortBy {
            it.productCode
        }
        additionalProductList.sortBy {
            it.name
        }

        activity.onNodeWithTag("checkInFilterDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("اضافی").performClick()
        activity.waitForIdle()

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(additionalProductList[i].KBarCode)
                assertTextContains(additionalProductList[i].name)
                assertTextContains("موجودی: " + additionalProductList[i].draftNumber)
                assertTextContains("اضافی: " + additionalProductList[i].conflictNumber)
            }
        }
    }

    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading || activity.activity.rf.scanning) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    //Simulating user scan rfid tags
    private fun epcScan(products: MutableList<String>) {
        activity.activity.rf.epcs.clear()
        for (i in 0 until products.size) {
            activity.activity.rf.epcs.add(products[i])
        }
        Log.e("rf epcs", activity.activity.rf.epcs.toList().toString())

        //down button pressed
        activity.activity.scanTrigger()
        activity.waitForIdle()
        Thread.sleep(3000)
        activity.waitForIdle()
        activity.activity.scanTrigger()
        activity.waitForIdle()
    }

    //Recreate activity
    private fun restart() {
        activity.activity.runOnUiThread {
            activity.activity.recreate()
        }
        waitForFinishLoading()
    }

    //Clear user scanned data by pressing delete button
    private fun clearUserData() {
        activity.onNodeWithTag("CheckInTestTag").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("confirm").performClick()
        activity.waitForIdle()
    }

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    //Product scanning simulation, get list of barcodes for input
    private fun barcodeArrayScan(products: MutableList<Product>) {
        products.forEach {
            if (it.draftNumber >= 3) {
                for (i in 0 until it.draftNumber - 3) {
                    barcodeScan(it.KBarCode)
                }
            } else if (it.draftNumber == 2) {
                for (i in 0 until it.draftNumber + 3) {
                    barcodeScan(it.KBarCode)
                }
            } else {
                for (i in 0 until it.draftNumber + 1) {
                    barcodeScan(it.KBarCode)
                }
            }
        }
    }

}