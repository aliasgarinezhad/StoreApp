package reader.features

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.features.shelf.view.ShelfEnter
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class ShelfEntryTest {

    @get:Rule
    var activity = createAndroidComposeRule<ShelfEnter>()

    @Test
    fun test() {

        waitForFinishLoading()
        barcodeScan("SHAR010103")
        waitForFinishLoading()
        /*activity.waitForIdle()
        activity.onAllNodesWithTag("items")[0].apply {
            assertTextContains("پلیور یقه هفت")
            assertTextContains("اسکن: 0")
            assertTextContains("04591001J-2630-M")
        }
        activity.onNodeWithText("اسکن: 1").assertExists()*/
        Thread.sleep(5000)
        barcodeScan("J31551711204038002")
        waitForFinishLoading()
        barcodeScan("J31551711204038002")
        waitForFinishLoading()
        Thread.sleep(5000)
        activity.onNodeWithText("بررسی و ارسال").performClick()
        activity.waitForIdle()
        waitForFinishLoading()
        Thread.sleep(5000)
        /*
        barcodeScan("SHITRC500200200")
        activity.waitForIdle()
        activity.onNodeWithText("مجموع: 0").assertExists()
        barcodeScan("04591001J-2630-M")
        activity.waitForIdle()
        activity.onNodeWithText("مجموع: 1").assertExists()
        activity.onNodeWithText("بررسی و ارسال").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("تایید نهایی").performClick()
        activity.waitForIdle()
        waitForFinishLoading()
        activity.activity.uiList.isEmpty()
*/
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
}