//package com.jeanwest.reader.view
//
//import android.view.KeyEvent
//import androidx.compose.ui.test.assertTextContains
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
//import androidx.compose.ui.test.onAllNodesWithTag
//import androidx.compose.ui.test.onAllNodesWithText
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import com.jeanwest.reader.models.Product
//import com.jeanwest.reader.models.StockDraftRequest
//import com.jeanwest.reader.models.StockDraftRequestItem
//import com.jeanwest.reader.testData.cartonProducts
//import org.json.JSONArray
//import org.junit.Rule
//import org.junit.Test
//
//class StockDraftRequestConfirmTest {
//
//    @get:Rule
//    val activity = createAndroidComposeRule<StockDraftRequestConfirm>()
//
//    @Test
//    fun test() {
//
//        start()
//        val kbarcodes = mutableListOf<String>()
//        val productJson = JSONArray(cartonProducts)
//
//        for (i in 0 until productJson.length()) {
//            kbarcodes.add(
//                productJson.getJSONObject(i).getString("kbarcode")
//            )
//        }
//
//        if (activity.activity.step == 1) {
//
//            activity.onNodeWithText("پایان اسکن").performClick()
//            activity.waitForIdle()
//            if (activity.activity.numberOfScanned != 0) {
//                activity.onNodeWithText("خیر، نتایج پاک شوند").performClick()
//                activity.waitForIdle()
//            }
//            activity.onNodeWithText("کسری").assertDoesNotExist()
//        }
//
//        waitForFinishLoading()
//
//        activity.onNodeWithText("درخواست: " + "114200012901").performClick()
//        waitForFinishLoading()
//
//        activity.onAllNodesWithText("کسری: ${stockDraftRequest.sumOfRequestedItems}")[0].assertExists()
//        activity.onNodeWithText("اضافی: 0").assertExists()
//        activity.onNodeWithText("اسکن: 0").assertExists()
//
//        activity.activity.inputProducts.forEach {
//            assert(it.value.product.draftNumber == kbarcodes.count { it1 ->
//                it.value.product.KBarCode == it1
//            })
//        }
//
//        val products = mutableListOf<StockDraftRequestItem>()
//        products.addAll(activity.activity.inputProducts.values)
//        products.sortBy {
//            it.product.productCode
//        }
//        products.sortBy {
//            it.product.name
//        }
//
//        for (i in 0 until 3) {
//
//            activity.onAllNodesWithTag("items")[i].apply {
//                assertTextContains(products[i].product.KBarCode)
//                assertTextContains(products[i].product.name)
//                assertTextContains("موجودی: " + products[i].product.draftNumber)
//                assertTextContains("کسری: " + products[i].product.draftNumber)
//            }
//        }
//
//        clearUserData()
//        activity.onNodeWithTag("scanTypeDropDownList").performClick()
//        activity.waitForIdle()
//        activity.onNodeWithText("بارکد").performClick()
//        activity.waitForIdle()
//        barcodeScan("123456")
//        barcodeArrayScan(products.subList(0, 3))
//        restart()
//        checkResults(products.subList(0, 3))
//
//        restart()
//        clearUserData()
//        epcScan(products.subList(0, 3))
//
//        activity.onNodeWithText("پایان اسکن").performClick()
//        activity.waitForIdle()
//        if (activity.activity.numberOfScanned != 0) {
//            activity.onNodeWithText("بله").performClick()
//            activity.waitForIdle()
//        }
//
//        for (i in 0 until 3) {
//
//            activity.onAllNodesWithTag("items")[i].apply {
//                assertTextContains(activity.activity.scannedProducts.values.toMutableList()[i].KBarCode)
//                assertTextContains(activity.activity.scannedProducts.values.toMutableList()[i].name)
//                assertTextContains("سایز: " + activity.activity.scannedProducts.values.toMutableList()[i].size)
//                assertTextContains("اسکن: " + kbarcodes.count { it1 ->
//                    activity.activity.scannedProducts.values.toMutableList()[i].KBarCode == it1
//                })
//            }
//        }
//    }
//
//    private fun checkResults(products: MutableList<StockDraftRequestItem>) {
//
//        var additionalNumber = 0
//        var scannedNumber = 0
//
//        products.forEach {
//
//            if (it.product.draftNumber >= 3) {
//
//                activity.activity.productConflicts.forEach { it1 ->
//                    if (it1.KBarCode == it.product.KBarCode) {
//                        assert(it1.conflictNumber == 3 && it1.conflictType == "کسری")
//                        scannedNumber += it1.scannedNumber
//                    }
//                }
//            } else if (it.product.draftNumber == 2) {
//
//                activity.activity.productConflicts.forEach { it1 ->
//                    if (it1.KBarCode == it.product.KBarCode) {
//                        assert(it1.conflictNumber == 3 && it1.conflictType == "اضافی")
//                        additionalNumber += 3
//                        scannedNumber += it1.scannedNumber
//                    }
//                }
//            } else {
//
//                activity.activity.productConflicts.forEach { it1 ->
//                    if (it1.KBarCode == it.product.KBarCode) {
//                        assert(it1.conflictNumber == 1 && it1.conflictType == "اضافی")
//                        additionalNumber += 1
//                        scannedNumber += it1.scannedNumber
//                    }
//                }
//            }
//        }
//
//        activity.onAllNodesWithText("کسری: ${stockDraftRequest.sumOfRequestedItems - (scannedNumber - additionalNumber)}")[0].assertExists()
//        activity.onNodeWithText("اضافی: $additionalNumber").assertExists()
//        activity.onNodeWithText("اسکن: $scannedNumber").assertExists()
//
//        val shortageProductList = activity.activity.productConflicts.filter {
//            it.conflictType == "کسری"
//        }.toMutableList()
//
//        shortageProductList.sortBy {
//            it.productCode
//        }
//        shortageProductList.sortBy {
//            it.name
//        }
//
//        val forLoopMaxValue = if (shortageProductList.size > 3) 3 else shortageProductList.size
//        for (i in 0 until forLoopMaxValue) {
//
//            activity.onAllNodesWithTag("items")[i].apply {
//                assertTextContains(shortageProductList[i].KBarCode)
//                assertTextContains(shortageProductList[i].name)
//                assertTextContains("موجودی: " + shortageProductList[i].draftNumber)
//                assertTextContains("کسری: " + shortageProductList[i].conflictNumber)
//            }
//        }
//
//        val additionalProductList = activity.activity.productConflicts.filter {
//            it.conflictType == "اضافی"
//        }.toMutableList()
//
//        additionalProductList.sortBy {
//            it.productCode
//        }
//        additionalProductList.sortBy {
//            it.name
//        }
//
//        activity.onNodeWithTag("checkInFilterDropDownList").performClick()
//        activity.waitForIdle()
//        activity.onNodeWithText("اضافی").performClick()
//        activity.waitForIdle()
//
//        for (i in 0 until 1) {
//
//            activity.onAllNodesWithTag("items")[i].apply {
//                assertTextContains(additionalProductList[i].KBarCode)
//                assertTextContains(additionalProductList[i].name)
//                assertTextContains("موجودی: " + additionalProductList[i].draftNumber)
//                assertTextContains("اضافی: " + additionalProductList[i].conflictNumber)
//            }
//        }
//    }
//
//    private fun waitForFinishLoading() {
//        activity.waitForIdle()
//        while (activity.activity.loading || activity.activity.rf.scanning) {
//            Thread.sleep(200)
//            activity.waitForIdle()
//        }
//    }
//
//    private fun epcScan(products: MutableList<StockDraftRequestItem>) {
//
//        activity.activity.rf.epcs.clear()
//        activity.activity.rf.epcs.add("30123456789")
//
//        products.forEach {
//            for (i in 0 until it.product.draftNumber) {
//                activity.activity.rf.epcs.add(epcGenerator(48, 0, 0, 101, it.product.rfidKey, i.toLong()))
//            }
//        }
//
//        activity.activity.onKeyDown(280, KeyEvent(KeyEvent.ACTION_DOWN, 280))
//        activity.waitForIdle()
//
//        Thread.sleep(1000)
//        activity.waitForIdle()
//
//        activity.activity.onKeyDown(280, KeyEvent(KeyEvent.ACTION_DOWN, 280))
//        activity.waitForIdle()
//        waitForFinishLoading()
//    }
//
//    private fun epcGenerator(
//        header: Int,
//        filter: Int,
//        partition: Int,
//        company: Int,
//        item: Long,
//        serial: Long
//    ): String {
//
//        var tempStr = java.lang.Long.toBinaryString(header.toLong())
//        val headerStr = String.format("%8s", tempStr).replace(" ".toRegex(), "0")
//        tempStr = java.lang.Long.toBinaryString(filter.toLong())
//        val filterStr = String.format("%3s", tempStr).replace(" ".toRegex(), "0")
//        tempStr = java.lang.Long.toBinaryString(partition.toLong())
//        val positionStr = String.format("%3s", tempStr).replace(" ".toRegex(), "0")
//        tempStr = java.lang.Long.toBinaryString(company.toLong())
//        val companynumberStr = String.format("%12s", tempStr).replace(" ".toRegex(), "0")
//        tempStr = java.lang.Long.toBinaryString(item)
//        val itemNumberStr = String.format("%32s", tempStr).replace(" ".toRegex(), "0")
//        tempStr = java.lang.Long.toBinaryString(serial)
//        val serialNumberStr = String.format("%38s", tempStr).replace(" ".toRegex(), "0")
//        val epcStr =
//            headerStr + positionStr + filterStr + companynumberStr + itemNumberStr + serialNumberStr // binary string of EPC (96 bit)
//
//        tempStr = epcStr.substring(0, 64).toULong(2).toString(16)
//        val epc0To64 = String.format("%16s", tempStr).replace(" ".toRegex(), "0")
//        tempStr = epcStr.substring(64, 96).toULong(2).toString(16)
//        val epc64To96 = String.format("%8s", tempStr).replace(" ".toRegex(), "0")
//
//        return epc0To64 + epc64To96
//    }
//
//    private fun restart() {
//        activity.activity.runOnUiThread {
//            activity.activity.recreate()
//        }
//        waitForFinishLoading()
//    }
//
//    private fun clearUserData() {
//        activity.onNodeWithTag("CheckInTestTag").performClick()
//        activity.waitForIdle()
//        activity.onNodeWithText("بله").performClick()
//        activity.waitForIdle()
//    }
//
//    private val stockDraftRequest = StockDraftRequest(
//        number = 114200012901,
//        sumOfRequestedItems = 4,
//        destination = "IT",
//        source = "",
//        sumOfControlledItems = 0,
//        sumOfFoundItems = 0,
//    )
//
//    private fun barcodeArrayScan(products: MutableList<StockDraftRequestItem>) {
//        products.forEach {
//            if (it.product.draftNumber >= 3) {
//                for (i in 0 until it.product.draftNumber - 3) {
//                    barcodeScan(it.product.KBarCode)
//                }
//            } else if (it.product.draftNumber == 2) {
//                for (i in 0 until it.product.draftNumber + 3) {
//                    barcodeScan(it.product.KBarCode)
//                }
//            } else {
//                for (i in 0 until it.product.draftNumber + 1) {
//                    barcodeScan(it.product.KBarCode)
//                }
//            }
//        }
//    }
//
//    private fun barcodeScan(barcode: String) {
//        activity.activity.barcode.barcode = barcode
//        activity.activity.barcode.scannedBarcodes.add(barcode)
//        activity.activity.barcode.getBarcode(barcode)
//        waitForFinishLoading()
//    }
//
//    private fun start() {
//        waitForFinishLoading()
//        activity.activity.runOnUiThread {
//            activity.activity.recreate()
//        }
//        waitForFinishLoading()
//    }
//}