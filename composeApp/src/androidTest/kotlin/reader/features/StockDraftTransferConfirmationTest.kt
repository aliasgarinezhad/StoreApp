package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.jeanwest.reader.features.logistic.view.StockDraftTransferConfirmation
import org.junit.Rule
import org.junit.Test

internal class StockDraftTransferConfirmationTest {

    @get:Rule
    var activity = createAndroidComposeRule<StockDraftTransferConfirmation>()

    @Test
    fun stockDraftTransferConfirmationTest1() {

        waitForFinishLoading()

        val itemSize = activity.activity.stockDraftTransfers.size

        for (i in 0 until itemSize) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains("نام راننده: " + activity.activity.stockDraftTransfers.keys.toList()[i])
                assertTextContains("تعداد حواله ها: " + activity.activity.stockDraftTransfers.values.toList()[i].size)
            }
        }

        if (itemSize > 0) {
            activity.onAllNodesWithTag("items")[1].performClick()
            activity.waitForIdle()
            activity.onNodeWithTag("alertDialogInput")
                .performTextInput(activity.activity.memory.erpData.drivers[activity.activity.stockDraftTransfers.keys.toList()[1]].toString())
            activity.waitForIdle()
            activity.onNodeWithTag("alertBtn").performClick()
            waitForFinishLoading()

            val stockDraftIDs = activity.activity.stockDrafts.keys.toList()

            for (i in 0 until stockDraftIDs.size/2) {
                barcodeScan(stockDraftIDs[i])
            }

            for (i in stockDraftIDs.size/2 until stockDraftIDs.size) {
                activity.onNodeWithTag("TextField").performTextClearance()
                activity.onNodeWithTag("TextField").performTextInput(stockDraftIDs[i])
                activity.onNodeWithTag("TextField").performImeAction()
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
}