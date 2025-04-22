package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.jeanwest.reader.features.logistic.view.StockDraftTransfer
import com.jeanwest.reader.testData.stockDraftProperties
import org.junit.Rule
import org.junit.Test

class StockDraftTransferTest {

    @get:Rule
    val activity = createAndroidComposeRule<StockDraftTransfer>()

    @Test
    fun test() {

        start()
        clearUserData()
        restart()

        barcodeScan(stockDraftProperties.number.toString())
        activity.onNodeWithTag("TextField").performTextClearance()
        activity.onNodeWithTag("TextField").performTextInput("114028")
        activity.onNodeWithTag("TextField").performImeAction()
        waitForFinishLoading()
        barcodeScan("114028")

        for (i in 0 until 2) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains("حواله: " + activity.activity.stockDraftUiList[i].number)
                assertTextContains("تعداد کالاها: " + activity.activity.stockDraftUiList[i].numberOfItems)
                assertTextContains("تاریخ: " + activity.activity.stockDraftUiList[i].date)
                assertTextContains("از: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.stockDraftUiList[i].source.toString()])
                assertTextContains("شرح: " + activity.activity.stockDraftUiList[i].specification)
                assertTextContains("به: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.stockDraftUiList[i].destination.toString()])
                assertTextContains("تگ RFID: " + if (activity.activity.stockDraftUiList[i].epcsToPrimaryKeysMap.isNotEmpty()) "دارد" else "ندارد")
            }
        }
    }

    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    private fun restart() {
        activity.activity.runOnUiThread {
            activity.activity.recreate()
        }
        waitForFinishLoading()
    }

    private fun clearUserData() {
        val stockDraftIDsTemp = mutableListOf<String>()
        stockDraftIDsTemp.addAll(activity.activity.stockDraftIDs)
        stockDraftIDsTemp.forEach {
            activity.activity.clear(it)
            waitForFinishLoading()
        }
    }

    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    private fun start() {
        activity.runOnUiThread {
            activity.activity.recreate()
        }
        waitForFinishLoading()
    }
}