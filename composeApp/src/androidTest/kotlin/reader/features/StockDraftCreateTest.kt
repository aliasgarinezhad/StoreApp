package reader.features

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
import com.jeanwest.reader.features.stockDraft.view.StockDraftCreate
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.testData.testBarcodesStock
import org.junit.Rule
import org.junit.Test

class StockDraftCreateTest {

    @get:Rule
    val activity = createAndroidComposeRule<StockDraftCreate>()

    //RUN REFILL TEST BEFOR RUN THIS TEST(to get products to use in this test)

    @Test
    fun testWithBarcode() {

        //The number of products to be compared
        val testProductsNumber = 10

        //clear scanned product by clear used list
        clearUserData()
        restart()

        //Get the Refill products for use in test
        val products = createTestProducts().subList(0, 10)

        activity.onAllNodesWithText("هنوز کالایی برای ثبت حواله اسکن نکرده اید")[0].assertExists()

        val scannedProducts = mutableListOf<Product>()
        products.forEach {
            scannedProducts.add(it)
        }

        barcodeScan("123456")
        activity.onAllNodesWithText("مشخصات بارکد 123456 یافت نشد.")[0].assertExists()
        activity.onNodeWithText("متوجه شدم").performClick()

        barcodeArrayScan(testProductsNumber, products)

        restart()
        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()

        assert(activity.activity.products.size == testProductsNumber)

        activity.activity.products.forEach {
            assert(it.scannedBarcodeNumber == 2)
        }

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("اسکن: " + "2")
            }
        }

        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("TextField").performTextInput("test")
        activity.onNodeWithTag("TextField").performImeAction()
//        activity.onNodeWithText("انتخاب مقصد").performClick()
//        activity.waitForIdle()
//        activity.onNodeWithText("IT").performClick()
//        activity.waitForIdle()
        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("تایید").performClick()
        activity.waitForIdle()
        waitForFinishLoading()
        activity.onAllNodesWithText("حواله با شماره", true)[0].assertExists()
        activity.waitForIdle()
        activity.onNodeWithTag("alertBtn").performClick()
        Thread.sleep(3000)
    }

    @Test
    fun testWithEpc() {

        //The number of products to be compared
        val testProductsNumber = 10

        //clear scanned product by clear used list
        clearUserData()
        restart()
        //Get the Refill products for use in test
        val products = createTestProducts().subList(0, 10)

        activity.onAllNodesWithText("هنوز کالایی برای ثبت حواله اسکن نکرده اید")[0].assertExists()

        val scannedProducts = mutableListOf<Product>()
        products.forEach {
            scannedProducts.add(it)
        }

        barcodeScan("123456")
        activity.onAllNodesWithText("مشخصات بارکد 123456 یافت نشد.")[0].assertExists()

        barcodeArrayScan(testProductsNumber, products)

        restart()
        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()

        assert(activity.activity.products.size == testProductsNumber)

        activity.activity.products.forEach {
            assert(it.scannedBarcodeNumber == 2)
        }

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("اسکن: " + "2")
            }
        }

        scannedProducts.clear()
        scannedProducts.addAll(activity.activity.uiList)

        restart()
        clearUserData()
        activity.onNodeWithText("بارکد").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("RFID").performClick()
        activity.waitForIdle()

        epcScan(scannedProducts.subList(0, testProductsNumber))

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("اسکن: " + "2")
            }
        }

        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("TextField").performTextInput("test")
        activity.onNodeWithTag("TextField").performImeAction()
        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("تایید").performClick()
        activity.waitForIdle()
        waitForFinishLoading()
        activity.onAllNodesWithText("حواله با شماره", true)[0].assertExists()
        activity.waitForIdle()
        activity.onNodeWithTag("alertBtn").performClick()
        Thread.sleep(3000)
    }

    @Test
    fun testWithManyBarcodes(){
        bArrayScan(testBarcodesStock.size, testBarcodesStock)
    }
    //Recreate activity
    private fun restart() {
        activity.activity.runOnUiThread {
            activity.activity.recreate()
        }
        waitForFinishLoading()
    }

    //Simulating user scan rfid tags
    private fun epcScan(products: MutableList<Product>) {

        activity.activity.rf.epcs.clear()
        activity.activity.rf.epcs.add("30123456789")

        products.forEach {
            for (i in 0 until 2) {
                activity.activity.rf.epcs.add(epcGenerator(48, 0, 0, 101, it.rfidKey, i.toLong()))
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

    //Create fake epc for create stockDraft by RFID
    private fun epcGenerator(
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
    }

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    //Product scanning simulation, get list of barcodes for input
    private fun barcodeArrayScan(number: Int, scannedProducts: MutableList<Product>) {
        for (i in 0 until number) {
            barcodeScan(scannedProducts[i].KBarCode)
            barcodeScan(scannedProducts[i].KBarCode)
        }
    }

    //Product scanning simulation, get list of barcodes for input
    private fun bArrayScan(number: Int, scannedProducts: MutableList<String>) {
        for (i in 0 until number) {
            barcodeScan(scannedProducts[i])
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

    //Clear user scanned data by clearing source list
    private fun clearUserData() {
        val products = mutableListOf<Product>()
        products.addAll(activity.activity.products)
        products.forEach {
            if (it.scannedNumber > 0) {
                activity.activity.clear(it)
                activity.waitForIdle()
            }
        }
    }

    //Get the Refill products for use in test
    private fun createTestProducts(): MutableList<Product> {
        val memory = PreferenceManager.getDefaultSharedPreferences(activity.activity)
        val type = object : TypeToken<MutableList<Product>>() {}.type
        val products: MutableList<Product> = Gson().fromJson(
            memory.getString("refillProductsForTest", ""),
            type
        ) ?: mutableListOf()
        return products
    }

}