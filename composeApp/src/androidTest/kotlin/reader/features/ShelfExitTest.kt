package reader.features

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.features.shelf.view.ShelfExit
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class ShelfExitTest {

    @get:Rule
    var activity = createAndroidComposeRule<ShelfExit>()

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
        activity.onNodeWithText("خروج").performClick()
        waitForFinishLoading()
        Thread.sleep(5000)
        /*activity.onNodeWithText("بررسی و ارسال").performClick()
        waitForFinishLoading()
        activity.onNodeWithText("بررسی و ارسال").assertDoesNotExist()
        waitForFinishLoading()
        activity.waitForIdle()
        activity.onNodeWithText("تاتیید نهایی").performClick()
        waitForFinishLoading()
        activity.onNodeWithText("اسکن: 0").assertExists()
        activity.onNodeWithText("مجموع: 0").assertExists()
        activity.activity.uiList.isEmpty()*/
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