package reader.features

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.jeanwest.reader.features.main.mainPage.view.MainActivity
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    var activity = createAndroidComposeRule<MainActivity>()

    @Test
    fun test() {
        waitForFinishLoading()
        restart()
        if (activity.activity.viewModel.memory.user.token != "") {
            activity.onNodeWithTag("navigation").performClick()
            activity.waitForIdle()
            activity.onNodeWithText("خروج از حساب").performClick()
            activity.waitForIdle()
            restart()
        }
        activity.onAllNodesWithTag("TextField").assertCountEquals(2)
        activity.onAllNodesWithTag("TextField")[0].performTextClearance()
        activity.onAllNodesWithTag("TextField")[0].performTextInput("4016")
        activity.onAllNodesWithTag("TextField")[1].performTextClearance()
        activity.onAllNodesWithTag("TextField")[1].performTextInput("4016")
        activity.onNodeWithText("ورود").performClick()
        waitForFinishLoading()
        activity.onNodeWithTag("navigation").assertExists()
        restart()
        activity.onNodeWithTag("navigation").assertExists()
        activity.onNodeWithTag("navigation").performClick()
        activity.waitForIdle()
        activity.onNodeWithText("خروج از حساب").performClick()
        activity.waitForIdle()
        activity.onAllNodesWithTag("TextField").assertCountEquals(2)
        restart()
        activity.onAllNodesWithTag("TextField").assertCountEquals(2)
    }

    private fun restart() {
        activity.runOnUiThread {
            activity.activity.recreate()
        }
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