package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.features.refillStore.view.Refill2
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi
class RefillNewTest {

    @get:Rule
    val activity = createAndroidComposeRule<Refill2>()

    @Test
    fun test() {

        //clear scanned product by clear used list
        waitForFinishLoading()
        clearUserData()

        //The number of products to be compared
        val numberOfTestItems = 3

        //To make sure that the number of products received is more than test items
        assert(activity.activity.viewModel.refillProducts.size > numberOfTestItems)

        //To make sure that clearUserData() fun worked correctly and refill products received correctly
        activity.onNodeWithText("خطی: " + activity.activity.viewModel.refillProducts.size)
            .assertExists()
        activity.onNodeWithText("کل اسکن: 0").assertExists()

        //Compare that the displayed information is equal to the original list
        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.viewModel.uiList[i].KBarCode)
                assertTextContains(activity.activity.viewModel.uiList[i].name)
                assertTextContains("انبار: " + activity.activity.viewModel.uiList[i].wareHouseNumber)
            }
        }

        //simulating wrong barcode scan
        barcodeScan("123456")

        //simulating barcode scanning
        barcodeArrayScan(numberOfTestItems, activity.activity.viewModel.uiList)

        //To make sure that refill number update correctly
        activity.onNodeWithText("خطی: " + (activity.activity.viewModel.refillProducts.size - activity.activity.viewModel.uiList.filter {
            it.scannedNumber > 0
        }.size)).assertExists()

        //Simulating user pressed send to store button
        activity.onNodeWithTag("bottomBarButton").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("خطی: ").assertDoesNotExist()

        //Scanned number should be equals to number of items
        assert(activity.activity.viewModel.refillProducts.filter {
            it.scannedBarcodeNumber > 0
        }.size == numberOfTestItems)

        //To make sure scanned barcode function work correctly
        activity.activity.viewModel.refillProducts.filter {
            it.scannedBarcodeNumber > 0
        }.toMutableList().forEach {
            assert(it.scannedBarcodeNumber == 1)
        }

        waitForFinishLoading()

        //Compare that the displayed information is equal to the original list
        val filteredUiList = activity.activity.viewModel.uiList
        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(filteredUiList[i].KBarCode)
                assertTextContains(filteredUiList[i].name)
                assertTextContains("انبار: " + filteredUiList[i].wareHouseNumber)
            }
        }

        //Simulating user change dropDown filter
        //activity.onNodeWithTag("FilterDropDownList").performClick()
        //activity.waitForIdle()
        //activity.onAllNodesWithText(activity.activity.viewModel.departmentFilterList[2])[0].performClick()
        //activity.waitForIdle()
        //assert(activity.activity.viewModel.selectedDepartmentFilter == activity.activity.viewModel.departmentFilterList[2])
        activity.onNodeWithText("خطی: " + (activity.activity.viewModel.uiList.size - activity.activity.viewModel.uiList.filter {
            it.scannedNumber > 0
        }.size))
            .assertExists()
        activity.onNodeWithText("کل اسکن: ${activity.activity.viewModel.barcode.scannedBarcodes.size}")
            .assertExists()
        /*for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.viewModel.selectedDepartmentFilter)
            }
        }*/

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
            barcodeScan(scannedProducts[i].KBarCode)
        }
    }

    //Product scanning simulation, get single string as barcode for input
    private fun barcodeScan(barcode: String) {
        activity.activity.viewModel.barcode.barcode = barcode
        activity.activity.viewModel.barcode.scannedBarcodes.add(barcode)
        activity.activity.viewModel.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.viewModel.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    //Clear user scanned data by clearing source list
    private fun clearUserData() {
        val products = mutableListOf<Product>()
        products.addAll(activity.activity.viewModel.refillProducts)
        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                activity.activity.viewModel.clear(it)
                activity.waitForIdle()
            }
        }
    }

}