package reader.features

import android.view.KeyEvent
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.features.kiosk.view.Kiosk
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class KioskTest {

    @get:Rule
    var activity = createAndroidComposeRule<Kiosk>()

    @Test
    fun test() {

        waitForFinishLoading()

        //Get the Refill products for use in test
        val product = createTestProducts()[0]
        barcodeScan(product.KBarCode)
        waitForFinishLoading()

        //Compare that the displayed information is equal to the original list
        activity.onNodeWithText("کد فرعی: " + activity.activity.uiList[0].productCode).assertExists()
        activity.onNodeWithText("قیمت: " + activity.activity.uiList[0].originalPrice).assertExists()
        activity.onNodeWithText("قیمت فروش: " + activity.activity.uiList[0].salePrice).assertExists()

        val maxItems = if (activity.activity.uiList.size > 3) 3 else activity.activity.uiList.size
        for (i in 0 until maxItems) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains("رنگ: " + activity.activity.uiList[i].color)
                assertTextContains("سایز: " + activity.activity.uiList[i].size)
                assertTextContains("انبار: " + activity.activity.uiList[i].wareHouseNumber)
                assertTextContains("فروشگاه: " + activity.activity.uiList[i].storeNumber)
            }
        }

        //Test back button performance
        activity.activity.onKeyDown(4, KeyEvent(KeyEvent.ACTION_DOWN, 4))
        activity.waitForIdle()

        activity.onNodeWithTag("CustomTextField").performTextClearance()
        activity.onNodeWithTag("CustomTextField").performTextInput(createTestProducts()[1].KBarCode)
        activity.onNodeWithTag("CustomTextField").performImeAction()

        waitForFinishLoading()

        activity.onNodeWithText("کد فرعی: " + activity.activity.uiList[0].productCode).assertExists()
        activity.onNodeWithText("قیمت: " + activity.activity.uiList[0].originalPrice).assertExists()
        activity.onNodeWithText("قیمت فروش: " + activity.activity.uiList[0].salePrice)
            .assertExists()

        for (i in 0 until maxItems) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains("رنگ: " + activity.activity.uiList[i].color)
                assertTextContains("سایز: " + activity.activity.uiList[i].size)
                assertTextContains("انبار: " + activity.activity.uiList[i].wareHouseNumber)
                assertTextContains("فروشگاه: " + activity.activity.uiList[i].storeNumber)
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

    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading) {
            Thread.sleep(200)
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