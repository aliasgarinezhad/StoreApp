package reader.features

import android.util.Log
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.features.cardex.viewmodel.Cardex
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class CardexTest {

    @get:Rule
    var activity = createAndroidComposeRule<Cardex>()

//    @Test
//    fun test() {
//
//        waitForFinishLoading()
//        activity.onNodeWithText("بارکد کالا را اسکن یا در کادر جستجو وارد کنید").assertExists()
//
//        //Scan wrong barcode
//        val invalidBarcode = "123456"
//        barcodeScan(invalidBarcode)
//        waitForFinishLoading()
//        activity.onNodeWithText("مشخصات بارکد $invalidBarcode یافت نشد.").assertExists()
//        activity.onNodeWithText("باشه").performClick()
//
//        //Get the Refill products for use in test
//        val product = createTestProducts()[0]
//        barcodeScan(product.KBarCode)
//        waitForFinishLoading()
//
//        Log.e("barcode", product.KBarCode)
//        //Compare that the displayed information is equal to the original list
//        activity.onNodeWithText(activity.activity.viewModel.productDetails.name).assertExists()
//        activity.onNodeWithText(activity.activity.viewModel.productDetails.KBarCode).assertExists()
//        activity.onNodeWithText("موجودی فعلی: " + activity.activity.viewModel.uiListCardex[activity.activity.viewModel.uiListCardex.size - 1].qtyAfterDelivery)
//            .assertExists()
//        activity.onNodeWithText("رنگ: ${activity.activity.viewModel.productDetails.color}")
//            .assertExists()
//
//        val maxItems =
//            if (activity.activity.viewModel.uiListCardex.size > 3) 3 else activity.activity.viewModel.uiListCardex.size
//        for (i in 0 until maxItems) {
//            activity.onAllNodesWithTag("items")[i].apply {
//                assertTextContains("شماره: " + activity.activity.viewModel.uiListCardex[i].deliveryID)
//                assertTextContains("در سند: " + activity.activity.viewModel.uiListCardex[i].deliveryQty)
//                assertTextContains("قبل سند: " + activity.activity.viewModel.uiListCardex[i].qtyBeforeDelivery)
//                assertTextContains("نوع: " + activity.activity.viewModel.uiListCardex[i].deliveryTypeDetails)
//                assertTextContains("تاریخ: " + activity.activity.viewModel.uiListCardex[i].deliveryDateShamsi)
//            }
//        }
//        Thread.sleep(2000)
//    }

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
//        while (activity.activity.viewModel.loading) {
//            Thread.sleep(200)
//            activity.waitForIdle()
//        }
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