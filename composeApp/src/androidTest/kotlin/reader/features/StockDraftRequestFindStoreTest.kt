package reader.features

import android.util.Log
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.preference.PreferenceManager
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestFindStore
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

class StockDraftRequestFindStoreTest {

    @get:Rule
    val activity = createAndroidComposeRule<StockDraftRequestFindStore>()

    //RUN StockDraftRequestCreateStore TEST BEFOR RUN THIS TEST(to get stock draft number to use in this test)
    @Test
    fun test() {

        waitForFinishLoading()

        if (activity.activity.scanningMode) {
            activity.onNodeWithTag("bottomBarButton").performClick()
            activity.waitForIdle()
            activity.onNodeWithTag("notConfirm").performClick()
            activity.waitForIdle()
            waitForFinishLoading()
            assert(!activity.activity.scanningMode)
        }

        activity.onNodeWithText("شماره درخواست حواله را اسکن یا در کادر جستجو وارد کنید")
            .assertExists()


        //Scan wrong barcode
        val invalidBarcode = "123456"
        barcodeScan(invalidBarcode)
        waitForFinishLoading()
        activity.onNodeWithText("مشخصات حواله $invalidBarcode یافت نشد.").assertExists()
        activity.onNodeWithText("باشه").performClick()

        val stockDraftId = createTestProducts()
        activity.onNodeWithTag("CustomTextField").performTextClearance()
        activity.onNodeWithTag("CustomTextField").performTextInput(stockDraftId)
        activity.onNodeWithTag("CustomTextField").performImeAction()
        waitForFinishLoading()
        Log.e("stockDraftNumber", stockDraftId)

        barcodeArrayScan(activity.activity.uiList)
        val scannedBarcode = activity.activity.uiList.size * 50
        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("موجودی: ${activity.activity.uiList[i].draftNumber}")
                assertTextContains(activity.activity.uiList[i].conflictType + ":" + " " + activity.activity.uiList[i].conflictNumber)
            }
        }

        activity.onNodeWithText("اسکن: $scannedBarcode").performClick()
        activity.onNodeWithText("کسری: ${activity.activity.shortagesNumber}").performClick()


        activity.onNodeWithTag("bottomBarButton").performClick()
        waitForFinishLoading()
        activity.onNodeWithText("بله").performClick()
        activity.waitForIdle()
        waitForFinishLoading()
        activity.onNodeWithText("حواله با شماره ${activity.activity.finalStockdraftId} ایجاد شد.").assertExists()
        Thread.sleep(3000)
    }

    //Product scanning simulation, get list of barcodes for input
    private fun barcodeArrayScan(scannedProducts: MutableList<Product>) {
        for (i in 0 until scannedProducts.size) {
            repeat(50) {
                barcodeScan(scannedProducts[i].KBarCode)
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
        activity.waitForIdle()
    }

    //Get StockDraft Number from memory
    private fun createTestProducts(): String {
        val memory = PreferenceManager.getDefaultSharedPreferences(activity.activity)
        val stockDraftId: String = memory.getString("StockDraftRequestCreateStoreId", "") ?: ""
        return stockDraftId
    }

}