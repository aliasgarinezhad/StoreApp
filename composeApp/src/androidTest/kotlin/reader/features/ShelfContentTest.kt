package reader.features

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.features.shelf.view.ShelfContent
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class ShelfContentTest {

    @get:Rule
    var activity = createAndroidComposeRule<ShelfContent>()

    @Test
    fun test() {

        waitForFinishLoading()

        barcodeScan("SHITRC500200200")
        waitForFinishLoading()
        activity.waitForIdle()

        activity.onNodeWithText(activity.activity.viewModel.uiListProduct[0].name).assertExists()
        activity.onNodeWithText("سایز: " + activity.activity.viewModel.uiListProduct[0].size).assertExists()
        activity.onNodeWithText(activity.activity.viewModel.uiListProduct[0].KBarCode).assertExists().assertExists()
        activity.onNodeWithText("مجموع: 1").assertExists()
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
}