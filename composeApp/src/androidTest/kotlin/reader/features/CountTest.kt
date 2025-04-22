package reader.features

import androidx.compose.foundation.ExperimentalFoundationApi
import coil.annotation.ExperimentalCoilApi

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalCoilApi

class CountTest {

    /*@get:Rule
    var activity = createAndroidComposeRule<Count>()

    @Test
    fun test1() {

        start()
        var inputProductsNumber = 0
        waitForFinishLoading()

        val type = object : TypeToken<MutableList<Product>>() {}.type
        val products: MutableList<Product> = Gson().fromJson(
            productsJsonObject,
            type
        )

        products.forEach {
            repeat(it.draftNumber) { _ ->
                activity.activity.inputBarcodes.add(it.KBarCode)
                inputProductsNumber++
            }
        }

        activity.activity.syncInputItemsToServer()
        waitForFinishLoading()

        products.forEach {
            assert(activity.activity.inputProducts[it.KBarCode]!!.draftNumber == it.draftNumber)
        }

        products.clear()
        products.addAll(activity.activity.inputProducts.values)
        products.sortBy {
            it.productCode
        }
        products.sortBy {
            it.name
        }

        waitForFinishLoading()

        activity.onNodeWithText("کسری: $inputProductsNumber").assertExists()
        activity.onNodeWithText("اضافی: 0").assertExists()
        activity.onNodeWithText("اسکن: 0").assertExists()

        val productMapRFID = mutableListOf<Product>()
        val productMapBarcode = mutableListOf<Product>()
        products.subList(0, 20).forEach { it1 ->
            productMapBarcode.add(it1)
        }
        products.subList(20, products.size).forEach { it1 ->
            productMapRFID.add(it1)
        }

        epcScan(false, products)
        waitForFinishLoading()

        activity.onNodeWithText("کسری: 0").assertExists()
        activity.onNodeWithText("اضافی: 0").assertExists()
        activity.onNodeWithText("اسکن: $inputProductsNumber").assertExists()

        clear()
        waitForFinishLoading()
        epcScan(true, products)
        checkResults()

        clear()
        waitForFinishLoading()
        epcScan(true, productMapRFID)
        activity.onNodeWithTag("scanTypeDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بارکد").performClick()
        activity.waitForIdle()
        waitForFinishLoading()
        barcodeScan("123456")
        barcodeArrayScan(productMapBarcode)
        activity.onNodeWithTag("CountActivityFilterDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("کسری").performClick()
        activity.waitForIdle()
        checkResults()
    }

    private fun checkResults() {

        var shortagesNumber = 0
        var additionalNumber = 0
        var scannedNumber = 0

        waitForFinishLoading()

        activity.activity.inputProducts.forEach {

            scannedNumber += activity.activity.uiList[it.value.KBarCode]!!.scannedNumber
            if (it.value.draftNumber >= 3) {
                assert(activity.activity.uiList[it.value.KBarCode]!!.conflictNumber == 3) {
                    Log.e(
                        "error",
                        activity.activity.uiList[it.value.KBarCode]!!.conflictNumber.toString()
                    )
                    Log.e("error", activity.activity.uiList[it.value.KBarCode]!!.toString())
                }
                assert(activity.activity.uiList[it.value.KBarCode]!!.conflictType == "کسری")
                shortagesNumber += 3
            } else if (it.value.draftNumber == 2) {
                assert(activity.activity.uiList[it.value.KBarCode]!!.conflictNumber == 3)
                assert(activity.activity.uiList[it.value.KBarCode]!!.conflictType == "اضافی")
                additionalNumber += 3
            } else {
                assert(activity.activity.uiList[it.value.KBarCode]!!.conflictNumber == 1)
                assert(activity.activity.uiList[it.value.KBarCode]!!.conflictType == "اضافی")
                additionalNumber += 1
            }
        }

        activity.onAllNodesWithText("کسری: $shortagesNumber")[0].assertExists()
        activity.onNodeWithText("اضافی: $additionalNumber").assertExists()
        activity.onNodeWithText("اسکن: $scannedNumber").assertExists()

        val shortageProductList = activity.activity.uiList.values.filter {
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

        val additionalProductList = activity.activity.uiList.values.filter {
            it.conflictType == "اضافی"
        }.toMutableList()

        additionalProductList.sortBy {
            it.productCode
        }
        additionalProductList.sortBy {
            it.name
        }

        activity.onNodeWithTag("CountActivityFilterDropDownList").performClick()
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

    private fun barcodeArrayScan(productMapBarcode: MutableList<Product>) {
        productMapBarcode.forEach {
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

    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    private fun clear() {
        activity.onNodeWithTag("CountActivityClearButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بله").performClick()
        activity.waitForIdle()
    }

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

    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading || activity.activity.rf.scanning) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    private fun start() {
        val memory = SharedPreference(activity.activity, activity.activity)
        waitForFinishLoading()
        MainActivity.token = memory.user.token
        activity.activity.runOnUiThread {
            activity.activity.recreate()
        }
        waitForFinishLoading()
    }*/
}