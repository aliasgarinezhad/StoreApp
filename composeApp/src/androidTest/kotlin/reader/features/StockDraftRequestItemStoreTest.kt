package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestFindStore
import com.jeanwest.reader.models.Product
import org.junit.Rule
import org.junit.Test

class StockDraftRequestItemStoreTest {

    @get:Rule
    val activity = createAndroidComposeRule<StockDraftRequestFindStore>()

    @Test
    fun test() {

        // 4306819

        waitForFinishLoading()

        if (activity.activity.scanningMode) {
            activity.onNodeWithText("صدور حواله").performClick()
            activity.waitForIdle()
            activity.onNodeWithTag("notConfirm").performClick()
            activity.waitForIdle()
            waitForFinishLoading()
            assert(!activity.activity.scanningMode)
        }


        barcodeScan("0")
        waitForFinishLoading()

        var shortageNumber = 0
        activity.activity.inputProducts.forEach {
            shortageNumber += it.value.draftNumber
        }

        activity.onNodeWithText("کسری: $shortageNumber").assertExists()
        activity.onNodeWithText("اسکن: 0").assertExists()

        val isInDepo =
            activity.activity.memory.user.warehouses[activity.activity.memory.user.warehouseCode.toString()]?.contains(
                "دپو"
            )
                ?: false

        activity.activity.inputProducts.forEach {
            assert(it.value.draftNumber == if (isInDepo) it.value.wareHouseNumber else it.value.storeNumber)
            assert(it.value.draftNumber > 0)
        }

        val products = mutableListOf<Product>()
        products.addAll(activity.activity.inputProducts.values)
        products.sortBy {
            it.productCode
        }
        products.sortBy {
            it.name
        }

        for (i in 0 until 3) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains(activity.activity.uiList[i].KBarCode)
                assertTextContains(activity.activity.uiList[i].name)
                assertTextContains("موجودی: " + activity.activity.uiList[i].draftNumber)
                assertTextContains("کسری: " + activity.activity.uiList[i].draftNumber)
            }
        }

        barcodeScan("123456")
        activity.waitForIdle()
        activity.activity.barcode.scannedBarcodes.clear()

        barcodeScan("11531052J-2010-L")
        barcodeArrayScan(products)

        activity.onNodeWithText("کسری: 0").assertExists()
        activity.onNodeWithText("اسکن: $shortageNumber").assertExists()

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

    private fun barcodeArrayScan(scannedProducts: MutableList<Product>) {
        for (i in 0 until scannedProducts.size) {
            repeat(scannedProducts[i].draftNumber) {
                barcodeScan(scannedProducts[i].KBarCode)
                barcodeScan(scannedProducts[i].KBarCode)
            }
        }
    }

    private fun barcodeScan(barcode: String) {
        activity.activity.barcode.barcode = barcode
        activity.activity.barcode.scannedBarcodes.add(barcode)
        activity.activity.barcode.getBarcode(barcode)
        waitForFinishLoading()
    }
}