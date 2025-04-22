package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.features.refillStore.view.Refill
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi
class RefillTest {

    @get:Rule
    val activity = createAndroidComposeRule<Refill>()

    @Test
    fun test() {

        //clear scanned product by clear used list
        waitForFinishLoading()
        clearUserData()

        //The number of products to be compared
        val numberOfTestItems = 5

        //To make sure that the number of products received is more than test items
        assert(activity.activity.inputBarcodes.size > numberOfTestItems)

        //To make sure that clearUserData() fun worked correctly and refill products received correctly
        activity.onNodeWithText("خطی: " + activity.activity.inputBarcodes.size).assertExists()
        activity.onNodeWithText("کل اسکن: 0").assertExists()

        //Compare that the displayed information is equal to the original list
        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("انبار: " + activity.activity.uiList[i].wareHouseNumber)
            }
        }

        //simulating wrong barcode scan
        barcodeScan("123456")

        //simulating barcode scanning
        barcodeArrayScan(numberOfTestItems, activity.activity.refillProducts)

        //To make sure that refill number update correctly
        activity.onNodeWithText("خطی: " + (activity.activity.inputBarcodes.size - activity.activity.uiList.filter {
            it.scannedNumber > 0
        }.size)).assertExists()

        //Simulating user pressed send to store button
        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("خطی: ").assertDoesNotExist()

        //Scanned number should be equals to number of items
        assert(activity.activity.refillProducts.filter {
            it.scannedBarcodeNumber > 0
        }.size == numberOfTestItems)

        //To make sure scanned barcode function work correctly
        activity.activity.refillProducts.filter {
            it.scannedBarcodeNumber > 0
        }.toMutableList().forEach {
            if (it.wareHouseNumber > 1) {
                assert(it.scannedBarcodeNumber == 2)
            } else {
                assert(it.scannedBarcodeNumber == 1)
            }
        }

        //Compare that the displayed information is equal to the original list
        val filteredUiList = activity.activity.uiList.filter {
            it.scannedBarcodeNumber > 0
        }
        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(filteredUiList[i].KBarCode)
                assertTextContains(filteredUiList[i].name)
                assertTextContains("انبار: " + filteredUiList[i].wareHouseNumber)
                assertTextContains("اسکن: " + if (filteredUiList[i].wareHouseNumber > 1) 2 else 1)
            }
        }

        //Simulating user pressed back button
        activity.onNodeWithTag("back").performClick()
        waitForFinishLoading()

        //Simulating user change dropDown filter
        activity.onNodeWithTag("FilterDropDownList").performClick()
        activity.waitForIdle()
        activity.onNodeWithText(activity.activity.departmentFilterList[1]).performClick()
        activity.waitForIdle()
        assert(activity.activity.selectedDepartmentFilter == activity.activity.departmentFilterList[1])
        activity.onNodeWithText("خطی: " + (activity.activity.uiList.size - activity.activity.uiList.filter {
            it.scannedNumber > 0
        }.size))
            .assertExists()
        activity.onNodeWithText("کل اسکن: ${activity.activity.barcode.scannedBarcodes.size}")
            .assertExists()
        for (i in 0 until activity.activity.uiList.size) {
            activity.onAllNodesWithTag("items")[i].apply {
                assert(activity.activity.uiList[i].departmentName == activity.activity.selectedDepartmentFilter)
            }
        }

        //Final confirm to send products to store
        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()
        waitForFinishLoading()
        activity.onNodeWithTag("notConfirm").performClick()
    }


    //Product scanning simulation, get list of barcodes for input
    private fun barcodeArrayScan(number: Int, scannedProducts: MutableList<Product>) {
        for (i in 0 until number) {
            if (scannedProducts[i].wareHouseNumber > 1) {
                barcodeScan(scannedProducts[i].KBarCode)
                barcodeScan(scannedProducts[i].KBarCode)
            } else {
                barcodeScan(scannedProducts[i].KBarCode)
            }
        }
    }

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.allScannedBarcodes.add(barcode)
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

    //Clear user scanned data by clearing source list
    private fun clearUserData() {
        val products = mutableListOf<Product>()
        products.addAll(activity.activity.refillProducts)
        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                activity.activity.clear(it)
                activity.waitForIdle()
            }
        }
    }

}