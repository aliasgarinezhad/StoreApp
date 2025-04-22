package reader.features

import android.util.Log
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.jeanwest.reader.features.carton.view.CartonTransfer
import com.jeanwest.reader.models.Carton
import org.junit.Rule
import org.junit.Test

class CartonTransferTest {

    @get:Rule
    val activity = createAndroidComposeRule<CartonTransfer>()

    //See api result message manually
    @Test
    fun test() {

        waitForFinishLoading()
        clearUserData()
        restart()

        val cartons = mutableListOf("CN3114100045356", "CN3114100045354", "CN3114100045346")

        activity.onAllNodesWithText("هنوز کارتنی برای ارسال اسکن نکرده اید")[0].assertExists()

        //Simulating user scan wrong carton number
        barcodeScan("123456")
        activity.onNodeWithText("شماره کارتن نامعتبر است.").assertExists()
        activity.onNodeWithText("باشه").performClick()

        //Simulating user typed carton number manually
        activity.onNodeWithTag("TextField").performTextClearance()
        activity.onNodeWithTag("TextField").performTextInput("CN3114100045354")
        activity.onNodeWithTag("TextField").performImeAction()
        waitForFinishLoading()

        cartons.forEach {
            barcodeScan(it)
            waitForFinishLoading()
        }

        activity.onNodeWithText("جمع کارتن: " + cartons.size).assertExists()

        for (i in 0 until 3) {

            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].number)
                assertTextContains("انبار جاری: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.uiList[i].operationSource])
                assertTextContains("تنوع جنس: " + activity.activity.uiList[i].barcodeTable.distinct().size)
                assertTextContains("جمع اجناس: " + activity.activity.uiList[i].numberOfItems)
            }
        }

        restart()

        activity.onNodeWithText("ارسال کارتن ها").performClick()
        activity.waitForIdle()

        assert(activity.activity.cartons.size == 3)

        for (i in 0 until 3) {

            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].number)
                assertTextContains("انبار جاری: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.uiList[i].operationSource])
                assertTextContains("تنوع جنس: " + activity.activity.uiList[i].barcodeTable.distinct().size)
                assertTextContains("جمع اجناس: " + activity.activity.uiList[i].numberOfItems)
            }
        }

        activity.onNodeWithText("انتخاب مقصد").performClick()
        waitForFinishLoading()
        Log.e("des: ", activity.activity.sortedWarehouseTitlesList[0])
        activity.onNodeWithText(activity.activity.sortedWarehouseTitlesList[0]).performClick()
        waitForFinishLoading()
        activity.activity.driver = "تست RFID تست RFID"
        waitForFinishLoading()
        activity.onNodeWithTag("bottomBarButton").performClick()
        waitForFinishLoading()
        Thread.sleep(4000)
    }

    //Recreate activity
    private fun restart() {
        activity.activity.runOnUiThread {
            activity.activity.recreate()
        }
        waitForFinishLoading()
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
            Thread.sleep(2000)
            activity.waitForIdle()
        }
    }

    //Clear user scanned data by clearing source list
    private fun clearUserData() {
        val products = mutableListOf<Carton>()
        products.addAll(activity.activity.uiList)
        products.forEach {
            activity.activity.clear(it)
            activity.waitForIdle()
        }
    }

}