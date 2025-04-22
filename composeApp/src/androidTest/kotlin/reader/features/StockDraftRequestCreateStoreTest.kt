package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestCreateStore
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

class StockDraftRequestCreateStoreTest {

    @get:Rule
    val activity = createAndroidComposeRule<StockDraftRequestCreateStore>()

    //RUN REFILL TEST BEFOR RUN THIS TEST(to get products to use in this test)
    @Test
    fun test() {

        //clear scanned product by clear used list
        clearUserData()
        waitForFinishLoading()

        activity.onNodeWithTag("emptyBox").assertExists()

        var products = createTestProducts()
        products = products.subList(0, if (products.size > 50) 50 else products.size - 1)
        val scannedProducts = mutableListOf<Product>()
        products.forEach {
            if (scannedProducts.size < 10) {
                scannedProducts.add(it)
            }
        }

        //Scan wrong barcode
        barcodeScan("123456")
        activity.onNodeWithText("مشخصات بارکد 123456 یافت نشد.").assertExists()
        activity.onNodeWithText("باشه").performClick()

        barcodeArrayScan(products)

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("اسکن: 1")
            }
        }

        //restart()
        activity.onNodeWithTag("bottomBarButton").performClick()
        waitForFinishLoading()

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("اسکن: 1")
            }
        }

        activity.onNodeWithText("انتخاب نوع درخواست").performClick()
        activity.waitForIdle()
        activity.onNodeWithText(activity.activity.filteredStockDraftType[1]).performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("bottomBarButton").performClick()
        waitForFinishLoading()
        activity.onNodeWithText("باشه").performClick()
        Thread.sleep(2000)
    }

    //Product scanning simulation, get list of barcodes for input
    private fun barcodeArrayScan(scannedProducts: MutableList<Product>) {
        for (i in 0 until scannedProducts.size) {
            //    repeat(scannedProducts[i].wareHouseNumber / 3) {
            barcodeScan(scannedProducts[i].KBarCode)
            //barcodeScan(scannedProducts[i].KBarCode)
            //    }
        }
    }

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading || activity.activity.rf.scanning) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
        activity.waitForIdle()
    }

    //Clear user scanned data by clearing source list
    private fun clearUserData() {
        val products = mutableListOf<Product>()
        products.addAll(activity.activity.products)
        products.forEach {
            activity.activity.clear(it)
            activity.waitForIdle()
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