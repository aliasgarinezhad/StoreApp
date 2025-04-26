package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.jeanwest.reader.features.shelf.newShelfIn.view.CreateShelfInRequest
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

class CreateShelfInRequestTest {

    @get:Rule
    val activity = createAndroidComposeRule<CreateShelfInRequest>()

    //SET SOURCE WAREHOUSE ON IT BEFORE START TEST

    @Test
    fun testWithCarton() {

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
                assertTextContains(activity.activity.viewModel.cartonsUiList[i].number)
                assertTextContains("انبار جاری: " + activity.activity.viewModel.cartonsUiList[i].cartonSource)
                assertTextContains("تنوع جنس: " + activity.activity.viewModel.cartonsUiList[i].barcodeTable.distinct().size)
                assertTextContains("جمع اجناس: " + activity.activity.viewModel.cartonsUiList[i].numberOfItems)
            }
        }

        Thread.sleep(10000)
        waitForFinishLoading()
    }

    @Test
    fun testWithBarcode() {

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

        val products = mutableListOf("41531052J-2010-M", "41531052J-2010-L", "41531052J-2010-XL")

        products.forEach {
            barcodeScan(it)
            waitForFinishLoading()
        }

        activity.onNodeWithText("مجموع: " + products.size).assertExists()

        for (i in 0 until products.size) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.viewModel.productsUiList[i].KBarCode)
                assertTextContains(activity.activity.viewModel.productsUiList[i].name)
                assertTextContains("موجودی: " + activity.activity.viewModel.productsUiList[i].wareHouseNumber)
                assertTextContains("تعداد: " + 1)
            }
        }
    }

    @Test
    fun mixedScanPreventionTest() {

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

        val scanList = mutableListOf("CN3114100045356", "41531052J-2010-M")

        scanList.forEach {
            barcodeScan(it)
            waitForFinishLoading()
        }

        activity.onNodeWithText("مجموع: " + 1).assertExists()

        activity.onAllNodesWithTag("items")[0].apply {
            assertTextContains(activity.activity.viewModel.cartonsUiList[0].number)
            assertTextContains("انبار جاری: " + activity.activity.viewModel.cartonsUiList[0].cartonSource)
            assertTextContains("تنوع جنس: " + activity.activity.viewModel.cartonsUiList[0].barcodeTable.distinct().size)
            assertTextContains("جمع اجناس: " + activity.activity.viewModel.cartonsUiList[0].numberOfItems)
        }
    }

    private fun barcodeScan(barcode: String) {
        activity.activity.viewModel.barcode.barcode = barcode
        activity.activity.viewModel.barcode.scannedBarcodes.add(barcode)
        activity.activity.viewModel.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }

    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.viewModel.loading) {
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
        clearCartons()
        clearProducts()
    }

    private fun clearCartons() {
        val products = mutableListOf<Carton>()
        products.addAll(activity.activity.viewModel.cartonsUiList)
        products.forEach {
            activity.activity.viewModel.deleteProduct(products.indexOf(it))
            activity.waitForIdle()
        }
    }

    private fun clearProducts() {
        val products = mutableListOf<Product>()
        products.addAll(activity.activity.viewModel.productsUiList)
        products.forEach {
            activity.activity.viewModel.deleteProduct(products.indexOf(it))
            activity.waitForIdle()
        }
    }
}