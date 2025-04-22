package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jeanwest.reader.features.stockDraft.view.StockDraftAttachEPCs
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.testData.primaryLightEPCs
import com.jeanwest.reader.testData.jootiEPCs
import com.jeanwest.reader.testData.stockDraftProducts
import com.jeanwest.reader.testData.stockDraftProperties
import org.json.JSONArray
import org.junit.Rule
import org.junit.Test

class StockDraftEditTest {

    @get:Rule
    val activity = createAndroidComposeRule<StockDraftAttachEPCs>()

    //IN THE END YOU SHOULD MANUALLY PRESS FINAL BUTTON
    //BECAUSE PROGRAMICALLY WE CAN'T COMPARE EPCS
    @Test
    fun test() {

        waitForFinishLoading()

        val kbarcodes = mutableListOf<String>()
        val productJson = JSONArray(stockDraftProducts)
        for (i in 0 until productJson.length()) {
            kbarcodes.add(
                productJson.getJSONObject(i).getString("kbarcode")
            )
        }

        if (activity.activity.scanningMode) {

            activity.onNodeWithText("پایان تروفالس").performClick()
            activity.waitForIdle()
            activity.onNodeWithTag("notConfirm").performClick()
            activity.waitForIdle()
            activity.onNodeWithText("کسری").assertDoesNotExist()
        }

        barcodeScan(stockDraftProperties.number.toString())
        waitForFinishLoading()

        activity.onNodeWithTag("scanTypeDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بارکد").performClick()
        activity.waitForIdle()

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

        activity.onNodeWithTag("scanTypeDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("RFID").performClick()
        activity.waitForIdle()

        epcScan(false, products)

        Thread.sleep(5000)
        activity.onNodeWithText("کسری: 0").assertExists()
        activity.onNodeWithText("اضافی: 0").assertExists()
        activity.onNodeWithText("اسکن: ${stockDraftProperties.numberOfItems}").assertExists()

        clearUserData()
        epcScan(true, products)
        checkResults()

        val epcProducts = mutableListOf<Product>()
        val barcodeProducts = mutableListOf<Product>()

        products.forEach {
            if ((it.brandName == "primaryLight" || it.brandName == "JootiJeans" || it.brandName == "Baleno")
                && !it.name.contains("جوراب")
                && !it.name.contains("عينك")
                && !it.name.contains("شاپينگ")
            ) {
                epcProducts.add(it)
            } else {
                barcodeProducts.add(it)
            }
        }

        clearUserData()
        waitForFinishLoading()
        epcScan(true, epcProducts)
        activity.onNodeWithTag("scanTypeDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بارکد").performClick()
        activity.waitForIdle()
        barcodeScan("123456")
        barcodeArrayScan(barcodeProducts)
        activity.onNodeWithTag("checkInFilterDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("کسری").performClick()
        activity.waitForIdle()
        checkResults()
    }

    @Test
    fun test2() {
        waitForFinishLoading()

        val kbarcodes = mutableListOf<String>()
        val productJson = JSONArray(stockDraftProducts)
        for (i in 0 until productJson.length()) {
            kbarcodes.add(
                productJson.getJSONObject(i).getString("kbarcode")
            )
        }

        if (activity.activity.scanningMode) {

            activity.onNodeWithText("پایان تروفالس").performClick()
            activity.waitForIdle()
            activity.onNodeWithTag("notConfirm").performClick()
            activity.waitForIdle()
            activity.onNodeWithText("کسری").assertDoesNotExist()
        }

        barcodeScan(stockDraftProperties.number.toString())
        waitForFinishLoading()

        val epcs = mutableListOf<String>()
        epcs.addAll(primaryLightEPCs)
        epcs.addAll(jootiEPCs)
        specialEPCSScan(epcs)

        activity.onNodeWithTag("checkInFilterDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("اضافی").performClick()
        activity.waitForIdle()

        activity.onNodeWithText("اضافی: ${epcs.size}").assertExists()

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("موجودی: " + activity.activity.uiList[i].draftNumber)
                assertTextContains("اضافی: " + activity.activity.uiList[i].conflictNumber)
            }
        }
    }


    //Compare displayed informations with main lists
    private fun checkResults() {

        var shortagesNumber = 0
        var additionalNumber = 0
        var scannedNumber = 0

        activity.activity.inputProducts.forEach {

            if (it.value.draftNumber >= 3) {
                activity.activity.productConflicts.forEach { it1 ->
                    if (it1.KBarCode == it.value.KBarCode) {
                        assert(it1.conflictNumber == 3 && it1.conflictType == "کسری")
                        shortagesNumber += 3
                        scannedNumber += it1.scannedNumber
                    }
                }
            } else if (it.value.draftNumber == 2) {
                activity.activity.productConflicts.forEach { it1 ->
                    if (it1.KBarCode == it.value.KBarCode) {
                        assert(it1.conflictNumber == 3 && it1.conflictType == "اضافی")
                        additionalNumber += 3
                        scannedNumber += it1.scannedNumber
                    }
                }
            } else {
                activity.activity.productConflicts.forEach { it1 ->
                    if (it1.KBarCode == it.value.KBarCode) {
                        assert(it1.conflictNumber == 1 && it1.conflictType == "اضافی")
                        additionalNumber += 1
                        scannedNumber += it1.scannedNumber
                    }
                }
            }
        }

        activity.onAllNodesWithText("کسری: $shortagesNumber")[0].assertExists()
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

    private fun specialEPCSScan(epcs: MutableList<String>) {

        activity.activity.rf.epcs.clear()

        epcs.forEach {
            activity.activity.rf.epcs.add(it)
        }

        activity.activity.scanTrigger()
        activity.waitForIdle()
        Thread.sleep(1000)
        activity.waitForIdle()
        activity.activity.scanTrigger()
        activity.waitForIdle()
        waitForFinishLoading()
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
    private fun epcScan(withDifference: Boolean, products: MutableList<Product>) {

        activity.activity.rf.epcs.clear()
        activity.activity.rf.epcs.add("30123456789")

        products.forEach {
            if (withDifference) {
                if (it.draftNumber >= 3) {
                    for (i in 0 until it.draftNumber - 3) {
                        activity.activity.rf.epcs.add(
                            epcGenerator(
                                48,
                                0,
                                0,
                                101,
                                it.rfidKey,
                                i.toLong()
                            )
                        )
                    }
                } else if (it.draftNumber == 2) {
                    for (i in 0 until it.draftNumber + 3) {
                        activity.activity.rf.epcs.add(
                            epcGenerator(
                                48,
                                0,
                                0,
                                101,
                                it.rfidKey,
                                i.toLong()
                            )
                        )
                    }
                } else {
                    for (i in 0 until it.draftNumber + 1) {
                        activity.activity.rf.epcs.add(
                            epcGenerator(
                                48,
                                0,
                                0,
                                101,
                                it.rfidKey,
                                i.toLong()
                            )
                        )
                    }
                }
            } else {
                for (i in 0 until it.draftNumber) {
                    activity.activity.rf.epcs.add(
                        epcGenerator(
                            48,
                            0,
                            0,
                            101,
                            it.rfidKey,
                            i.toLong()
                        )
                    )
                }
            }
        }
        activity.activity.scanTrigger()
        activity.waitForIdle()
        Thread.sleep(1000)
        activity.waitForIdle()
        activity.activity.scanTrigger()
        activity.waitForIdle()
        waitForFinishLoading()
    }

    //Clear user scanned data by clearing source list
    private fun clearUserData() {
        activity.onNodeWithTag("CheckInTestTag").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بله").performClick()
        activity.waitForIdle()
    }

    //Create fake epc for create stockDraft by RFID
    private fun epcGenerator(
        header: Int,
        filter: Int,
        partition: Int,
        company: Int,
        item: Long,
        serial: Long
    ): String {

        var tempStr = java.lang.Long.toBinaryString(header.toLong())
        val headerStr = String.format("%8s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(filter.toLong())
        val filterStr = String.format("%3s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(partition.toLong())
        val positionStr = String.format("%3s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(company.toLong())
        val companynumberStr = String.format("%12s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(item)
        val itemNumberStr = String.format("%32s", tempStr).replace(" ".toRegex(), "0")
        tempStr = java.lang.Long.toBinaryString(serial)
        val serialNumberStr = String.format("%38s", tempStr).replace(" ".toRegex(), "0")
        val epcStr =
            headerStr + positionStr + filterStr + companynumberStr + itemNumberStr + serialNumberStr // binary string of EPC (96 bit)

        tempStr = epcStr.substring(0, 64).toULong(2).toString(16)
        val epc0To64 = String.format("%16s", tempStr).replace(" ".toRegex(), "0")
        tempStr = epcStr.substring(64, 96).toULong(2).toString(16)
        val epc64To96 = String.format("%8s", tempStr).replace(" ".toRegex(), "0")

        return epc0To64 + epc64To96
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

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

}