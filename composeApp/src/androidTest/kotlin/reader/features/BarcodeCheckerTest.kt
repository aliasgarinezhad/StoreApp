package reader.features

import android.view.KeyEvent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.features.kiosk.view.BarcodeChecker
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class BarcodeCheckerTest {

    @get:Rule
    var activity = createAndroidComposeRule<BarcodeChecker>()

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
        activity.onNodeWithText("سایز: " + activity.activity.uiList[0].size).assertExists()
        activity.onNodeWithText("رنگ: " + activity.activity.uiList[0].color).assertExists()

        for (i in 0 until 5) {

            val departmentNumber = activity.activity.productDepartmentNumbers.keys.toList()[i]

            val text = activity.activity.memory.erpData.departments[departmentNumber] +
                    ": " + activity.activity.productDepartmentNumbers[departmentNumber]
            activity.onNodeWithText(text).assertExists()
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