package reader.features

/*
class CartonDetailsTest {

    @get:Rule
    val activity = createAndroidComposeRule<CartonDetails>()

    @Test
    fun test() {

        waitForFinishLoading()

        val kbarcodes = mutableListOf<String>()
        val productJson = JSONArray(cartonProducts)

        for (i in 0 until productJson.length()) {
            kbarcodes.add(
                productJson.getJSONObject(i).getString("kbarcode")
            )
        }

        barcodeScan(carton.number)
        waitForFinishLoading()

        activity.onNodeWithText("مجموع: ${carton.numberOfItems}").assertExists()
        activity.onNodeWithText("انبار جاری: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.cartonProperties.operationSource])
            .assertExists()

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
                assertTextContains("سایز: " + products[i].size)
            }
        }

        //Simulating user press back button
        activity.onNodeWithTag("back", useUnmergedTree = true).performClick()
        waitForFinishLoading()

        //Simulating user typed manually carton barcode
        activity.onNodeWithTag("CustomTextField").performTextClearance()
        activity.onNodeWithTag("CustomTextField").performTextInput(carton.number)
        activity.onNodeWithTag("CustomTextField").performImeAction()
        waitForFinishLoading()

        activity.onNodeWithText("مجموع: ${carton.numberOfItems}").assertExists()
        activity.onNodeWithText("انبار جاری: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.cartonProperties.operationSource])
            .assertExists()

        activity.activity.inputProducts.forEach {
            assert(it.value.draftNumber == kbarcodes.count { it1 ->
                it.value.KBarCode == it1
            })
        }

        products.clear()
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
                assertTextContains("سایز: " + products[i].size)
            }
        }
        // WE CAN NOT TEST PRINT LABEL BY CODE BECAUSE IT PRINT IN REAL WAREHOUSE
        // YOU HAVE TO TEST IT MANUALLY
    }


    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
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

 */