package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.jeanwest.reader.features.carton.view.AddOrRemoveCarton
import com.jeanwest.reader.models.Carton
import org.junit.Rule
import org.junit.Test

class AddOrRemoveCartonTest {

    @get:Rule
    val activity = createAndroidComposeRule<AddOrRemoveCarton>()

    //send all stuffs after test
    @Test
    fun test() {

        val testCage = "test"

        start()
        clearUserData()

        activity.onNodeWithTag("CustomTextField").performTextClearance()
        activity.onNodeWithTag("CustomTextField").performTextInput(testCage)
        activity.onNodeWithTag("CustomTextField").performImeAction()
        activity.waitForIdle()

        activity.onNodeWithTag("CustomTextField").assertDoesNotExist()
        waitForFinishLoading()
        activity.onNodeWithTag("emptyBox").assertExists()

        val cartons = mutableListOf("CN3114100045356", "CN3114100045354", "CN3114100045346")

        cartons.forEach {
            barcodeScan(it)
            barcodeScan(it)
            waitForFinishLoading()
        }

        activity.onNodeWithText("مجموع: " + cartons.size).assertExists()

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].number)
                assertTextContains("انبار جاری: " + activity.activity.memory.erpData.warehousesIDsToTitles[activity.activity.uiList[i].operationSource])
                assertTextContains("تنوع جنس: " + activity.activity.uiList[i].barcodeTable.distinct().size)
                assertTextContains("جمع اجناس: " + activity.activity.uiList[i].numberOfItems)
            }
        }

        activity.onNodeWithTag("FilterDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("خروج کارتن").performClick()
        activity.waitForIdle()

        Thread.sleep(10000)
        activity.onNodeWithTag("bottomBarButton").performClick()
        waitForFinishLoading()
        Thread.sleep(10000)
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

    private fun clearUserData() {
        val products = mutableListOf<Carton>()
        products.addAll(activity.activity.uiList)
        products.forEach {
            activity.activity.clear(it)
            activity.waitForIdle()
        }
    }
}