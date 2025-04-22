package reader.features

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.features.print.view.PrintPricePerProduct
import org.junit.Rule
import org.junit.Test

@ExperimentalCoilApi

class PrintPriceLabelPerProductTest {

    @get:Rule
    var activity = createAndroidComposeRule<PrintPricePerProduct>()

    @Test
    fun test() {
        waitForFinishLoading()
        getTestBarcodes()
        waitForFinishLoading()
        clickItems()
    }

    private fun clickItems() {
        for (i in 0 until activity.activity.viewModel.uiList.size) {
            activity.activity.viewModel.onProductClick(activity.activity.viewModel.uiList[i], i)
            waitForFinishLoading()
            activity.onNodeWithText("دستور پرینت لیبل قیمت با موفقیت ارسال شد.").assertExists()
        }
    }

    //Get the Refill products for use in test
    private fun getTestBarcodes() {
        val fakeList = createTestProducts().subList(0, 4)
        activity.activity.viewModel.uiList.addAll(fakeList)
    }

    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.viewModel.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }

    //Get the Refill products for use in test
    private fun createTestProducts(): MutableList<Product> {
        val memory = PreferenceManager.getDefaultSharedPreferences(activity.activity)
        val type = object : TypeToken<MutableList<Product>>() {}.type
        val products: MutableList<Product> = Gson().fromJson(
            memory.getString("refillProductsForTest", ""),
            type
        ) ?: mutableListOf()
        return products
    }
}