package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.features.print.view.PrintPriceLabel
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class PrintPriceLabelTest {

    @get:Rule
    var activity = createAndroidComposeRule<PrintPriceLabel>()

    @Test
    fun test() {
        waitForFinishLoading()
        compareData()
        waitForFinishLoading()
        scanFakeBarcodes()
        scanWrongProduct()
    }

    //Compare that the displayed information is equal to the original list
    private fun compareData() {
        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains("حواله: " + activity.activity.viewModel.uiList[i].number)
                assertTextContains("شرح: " + activity.activity.viewModel.uiList[i].specification)
                assertTextContains("تعداد کالاها: " + activity.activity.viewModel.uiList[i].numberOfItems)
            }
        }
    }

    private fun scanFakeBarcodes(){
        //Get the Refill products for use in test
        val products = createTestProducts()
        for (i in 0 until 3){
            barcodeScan(products[i].KBarCode)
            activity.onNodeWithText("دستور پرینت لیبل قیمت با موفقیت ارسال شد.").assertExists()
            activity.onNodeWithText("متوجه شدم").performClick()
        }
    }

    private fun scanWrongProduct(){
        barcodeScan("123456")
        activity.onNodeWithText("شماره حواله وارد شده معتبر نیست یا کاربر به آن دسترسی ندارد.").assertExists()
    }

    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.viewModel.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.viewModel.barcode.barcode = barcode
        activity.activity.viewModel.barcode.scannedBarcodes.add(barcode)
        activity.activity.viewModel.barcode.getBarcode(barcode)
        waitForFinishLoading()
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