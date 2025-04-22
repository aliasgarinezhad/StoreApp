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
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.features.kiosk.view.KioskCentralWarehouse
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class KioskCentralWarehouseTest {

    @get:Rule
    var activity = createAndroidComposeRule<KioskCentralWarehouse>()

    @Test
    fun test() {

        start()

        barcodeScan("11531052J-2010-L")
        waitForFinishLoading()

        checkResults()
        activity.activity.onKeyDown(4, KeyEvent(KeyEvent.ACTION_DOWN, 4))
        activity.waitForIdle()
        activity.onNodeWithTag("CustomTextField").performTextClearance()
        activity.onNodeWithTag("CustomTextField").performTextInput("11531052J-2010-L")
        activity.onNodeWithTag("CustomTextField").performImeAction()
        waitForFinishLoading()
        checkResults()
    }

    private fun checkResults() {
        activity.onNodeWithText(activity.activity.uiList[0].name).assertExists()
        activity.onNodeWithText("کد فرعی: " + activity.activity.uiList[0].productCode)
            .assertExists()
        activity.onNodeWithText("قیمت: " + activity.activity.uiList[0].originalPrice).assertExists()
        activity.onNodeWithText("قیمت فروش: " + activity.activity.uiList[0].salePrice)
            .assertExists()

        for (i in 0 until 2) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains("رنگ: " + activity.activity.uiList[i].color)
                assertTextContains("سایز: " + activity.activity.uiList[i].size)
                assertTextContains("موجودی: " + activity.activity.uiList[i].wareHouseNumber)
                assertTextContains(activity.activity.uiList[i].brandName)
            }
        }
    }

    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    private fun start() {
        waitForFinishLoading()
        activity.activity.runOnUiThread {
            activity.activity.recreate()
        }
        waitForFinishLoading()
    }
}