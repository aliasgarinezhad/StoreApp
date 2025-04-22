package reader.features

import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)

class InventoryTest {
/*
    @get:Rule
    var activity = createAndroidComposeRule<Inventory>()

    @Test
    fun test() {

        val warehouseCode = 1706

        if (activity.activity.viewModel.memory.user.warehouseCode != warehouseCode) {
            assert(false) { "warehouse code should be $warehouseCode" }
        }

        activity.onNodeWithText("شروع انبارگردانی").performClick()
        activity.waitForIdle()

        waitForFinishLoading()

        val products = mutableListOf<Product>()
        products.addAll(activity.activity.inventoryResult.values)
        var inputProductsNumber = 0
        products.filter { it.inventoryConflictType == "کسری" }.forEach {
            inputProductsNumber += it.inventoryNumber
        }

        activity.onNodeWithText("مغایرت ها").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("کسری: $inputProductsNumber").assertExists()
        activity.onNodeWithText("اضافی: 0").assertExists()

        for (i in 0 until 3) {

            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("موجودی: " + activity.activity.uiList[i].inventoryNumber)
                assertTextContains("کسری: " + activity.activity.uiList[i].inventoryNumber)
            }
        }

        activity.onNodeWithTag("back").performClick()

        epcScan(false, products)

        activity.onNodeWithText("مغایرت ها").performClick()
        activity.waitForIdle()

        activity.onNodeWithTag("FilterDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("اضافی").performClick()
        activity.waitForIdle()

        assert(activity.activity.inventoryProgress > 0.99F)
        assert(activity.activity.shortagesNumber < 100)
        assert(activity.activity.additionalNumber < 100)

        activity.onNodeWithTag("back").performClick()
        activity.waitForIdle()

        //Thread.sleep(50000)

        activity.onNodeWithText(
            String.format(
                "%.2f",
                activity.activity.inventoryProgress * 100
            ) + "%\n" + activity.activity.confirmedNumber + "/" + activity.activity.allInventoryNumber
        ).assertExists()

        activity.onNodeWithText("مغایرت ها").performClick()
        activity.waitForIdle()

        /*activity.onNodeWithText("پایان انبارگردانی").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("confirm").performClick()
        activity.waitForIdle()
        waitForFinishLoading()*/
    }

    @Test
    fun test3() {

        if (activity.activity.memory.user.warehouseCode != 1707) {
            assert(false) { "warehouse code should be 1707" }
        }

        activity.onNodeWithText("شروع انبارگردانی").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("confirm").performClick()
        activity.waitForIdle()
        waitForFinishLoading()

        activity.onNodeWithText("مغایرت ها").performClick()
        activity.waitForIdle()

        val products = mutableListOf<Product>()
        products.addAll(activity.activity.warehouseProductsWithDetails.values)

        activity.onNodeWithText("پایان انبارگردانی").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("notConfirm").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("back").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("شروع انبارگردانی").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("notConfirm").performClick()
        activity.waitForIdle()
        waitForFinishLoading()

        epcScan(true, products)

        waitForFinishLoading()

        activity.onNodeWithText("مغایرت ها").performClick()
        activity.waitForIdle()
        checkResults()
    }

    private fun clearScannedData() {
        activity.onNodeWithTag("clear").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("بله").performClick()
        activity.waitForIdle()
    }

    private fun waitForFinishLoading() {
        activity.waitUntil(240000) {
            !activity.activity.loading
        }
    }


    private fun checkResults() {

        activity.activity.inventoryResult.forEach {

            if (it.value.inventoryNumber >= 3) {
                assert(activity.activity.inventoryResult[it.value.primaryKey]!!.inventoryConflictNumber == -3) {
                    Log.e(
                        "test error",
                        activity.activity.inventoryResult[it.value.primaryKey].toString()
                    )
                }
                assert(activity.activity.inventoryResult[it.value.primaryKey]!!.inventoryConflictType == "کسری")
            } else if (it.value.inventoryNumber == 2) {
                assert(activity.activity.inventoryResult[it.value.primaryKey]!!.inventoryConflictNumber == 3)
                assert(activity.activity.inventoryResult[it.value.primaryKey]!!.inventoryConflictType == "اضافی")
            } else {
                assert(activity.activity.inventoryResult[it.value.primaryKey]!!.inventoryConflictNumber == 1)
                assert(activity.activity.inventoryResult[it.value.primaryKey]!!.inventoryConflictType == "اضافی")
            }
        }

        val shortageProductList = activity.activity.inventoryResult.values.filter {
            it.inventoryConflictType == "کسری"
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
                assertTextContains("موجودی: " + shortageProductList[i].inventoryNumber)
                assertTextContains("کسری: " + shortageProductList[i].inventoryConflictAbs)
            }
        }

        val additionalProductList = activity.activity.inventoryResult.values.filter {
            it.inventoryConflictType == "اضافی"
        }.toMutableList()

        additionalProductList.sortBy {
            it.productCode
        }
        additionalProductList.sortBy {
            it.name
        }

        activity.onNodeWithTag("FilterDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("اضافی").performClick()
        activity.waitForIdle()

        for (i in 0 until 2) {

            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(additionalProductList[i].KBarCode)
                assertTextContains(additionalProductList[i].name)
                assertTextContains("موجودی: " + additionalProductList[i].inventoryNumber)
                assertTextContains("اضافی: " + additionalProductList[i].inventoryConflictAbs)
            }
        }
    }

    private fun epcScan(withDifference: Boolean, rfidProducts: MutableList<Product>) {

        activity.activity.rf.epcs.clear()
        activity.activity.rf.epcs.add("111452833627900110000000")
        activity.activity.rf.epcs.add("111452833627900110000000")
        activity.activity.rf.epcs.add("111452833628600110000000")
        activity.activity.rf.epcs.add("111452833629300110000000")
        activity.activity.rf.epcs.add("111452833630900110000000")
        activity.activity.rf.epcs.add("111452833632300110000000")

        if (withDifference) {

            rfidProducts.forEach {

                if (it.inventoryNumber >= 3) {
                    for (i in 0 until it.inventoryNumber - 3) {
                        activity.activity.rf.epcs.add(
                            avakatanEPCGenerator(
                                48,
                                0,
                                0,
                                101,
                                it.rfidKey,
                                i.toLong()
                            )
                        )
                    }
                } else if (it.inventoryNumber == 2) {
                    for (i in 0 until it.inventoryNumber + 3) {
                        activity.activity.rf.epcs.add(
                            avakatanEPCGenerator(
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
                    for (i in 0 until it.inventoryNumber + 1) {
                        activity.activity.rf.epcs.add(
                            avakatanEPCGenerator(
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
        } else {

            rfidProducts.subList(0, rfidProducts.size/2).forEach {

                for (i in 0 until it.inventoryNumber) {

                    val jootiEPCOrNull = generateEPCInJootiFormatOrNull(it, i.toLong())
                    if (jootiEPCOrNull != null) {
                        activity.activity.rf.epcs.add(jootiEPCOrNull)
                    } else {
                        activity.activity.rf.epcs.add(
                            avakatanEPCGenerator(
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

            rfidProducts.subList(rfidProducts.size/2, rfidProducts.size -1).forEach {

                for (i in 0 until it.inventoryNumber) {

                    val jootiEPCOrNull = generateEPCInJootiFormatOrNull(it, i.toLong())
                    if (jootiEPCOrNull != null) {
                        activity.activity.rf.epcs.add(jootiEPCOrNull)
                    } else {
                        activity.activity.rf.epcs.add(
                            avakatanEPCGenerator(
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
        }

        activity.activity.onKeyDown(280, KeyEvent(KeyEvent.ACTION_DOWN, 280))
        activity.waitForIdle()

        Thread.sleep(1000)
        activity.waitForIdle()

        activity.activity.onKeyDown(280, KeyEvent(KeyEvent.ACTION_DOWN, 280))
        waitForFinishLoading()
    }

    private fun generateEPCInJootiFormatOrNull(product: Product, serialNumber: Long): String? {


        val size = when (product.size) {
            "S" -> "01"
            "M" -> "02"
            "L" -> "03"
            "XL" -> "04"
            "XXL" -> "05"
            "XXXL" -> "06"
            "4XL" -> "07"
            "5XL" -> "08"
            "6XL" -> "09"
            else -> product.size
        }

        if (size.toLongOrNull() == null || product.color.toLongOrNull() == null) {
            return null
        } else if ((product.productCode.startsWith("s") ||
                    product.productCode.startsWith("S")) &&
            product.productCode.substring(1, product.productCode.length).toLongOrNull() != null
        ) {

            var epcTemp = (product.productCode.substring(1) + product.color + size).toLong()
                .toHexString() + "0000000" + serialNumber.toHexString()

            epcTemp = String.format("%24s", epcTemp).replace(" ".toRegex(), "0")
            return epcTemp
        } else if ((product.productCode.endsWith("s") ||
                    product.productCode.endsWith("S")) &&
            product.productCode.substring(0, product.productCode.length - 1).toLongOrNull() != null
        ) {
            var epcTemp = (product.productCode.substring(
                0,
                product.productCode.length - 1
            ) + product.color + size).toLong()
                .toHexString() + "0000000" + serialNumber.toHexString()

            epcTemp = String.format("%24s", epcTemp).replace(" ".toRegex(), "0")
            return epcTemp
        } else if (product.productCode.toLongOrNull() == null) {
            return null
        } else {
            var epcTemp = (product.productCode + product.color + size).toLong()
                .toHexString() + "0000000" + serialNumber.toHexString()

            epcTemp = String.format("%24s", epcTemp).replace(" ".toRegex(), "0")
            return epcTemp
        }
    }

    private fun avakatanEPCGenerator(
        header: Int,
        filter: Int,
        partition: Int,
        company: Int,
        item: Long,
        serial: Long,
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

    }*/
}