package reader.features

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.features.stockDraft.view.StockDraftDetails
import org.junit.Rule
import org.junit.Test

class StockDraftDetailsTest {

    @get:Rule
    val activity = createAndroidComposeRule<StockDraftDetails>()

    //RUN CREATE STOCK DRAFT CREATE TEST BEFORE THIS TEST
    @Test
    fun test() {

        //To receive previos sended stock draft data
        val memory = PreferenceManager.getDefaultSharedPreferences(activity.activity)
        val type = object : TypeToken<MutableList<String>>() {}.type
        val stockDraftNumber: String = memory.getString("stockDraftNumber", "") ?: ""
        val barcode = Gson().fromJson(memory.getString("StockDraftBarcodeForTest", ""), type)
            ?: mutableListOf<String>()
        val totalShortage = barcode.size
        val testStockDraftFalse = "1234567"

        waitForFinishLoading()

        //Simulating user enter wrong stockDraft number
        activity.onNodeWithTag("CustomTextField").performTextInput(testStockDraftFalse)
        activity.onNodeWithTag("CustomTextField").performImeAction()
        waitForFinishLoading()
        activity.waitForIdle()

        //Simulating user enter correct stockDraft Number
        activity.onNodeWithText("شماره حواله را اسکن یا در کادر جستجو وارد کنید").assertExists()
        activity.onNodeWithTag("CustomTextField").performTextClearance()
        activity.onNodeWithTag("CustomTextField").performTextInput(stockDraftNumber)
        activity.onNodeWithTag("CustomTextField").performImeAction()
        waitForFinishLoading()
        activity.waitForIdle()
        activity.onNodeWithTag("CustomTextField").assertDoesNotExist()

        //Compare that the displayed information is equal to the original list
        activity.onNodeWithText("مجموع: $totalShortage").assertExists()
        activity.onNodeWithText("مبدا: IT(دپو)").assertExists()
        activity.onNodeWithText("مقصد: IT").assertExists()
        val compareSize = if (activity.activity.uiList.size > 3) 3 else activity.activity.uiList.size
        for (i in 0 until compareSize) {
            activity.onAllNodesWithTag("items")[i].apply {
                assertTextContains("موجودی: " + activity.activity.uiList[i].draftNumber)
            }
        }
    }

    //Simulating real user delay ,current delay time is 0.2 second
    private fun waitForFinishLoading() {
        activity.waitForIdle()
        while (activity.activity.loading) {
            Thread.sleep(200)
            activity.waitForIdle()
        }
    }
}