package com.jeanwest.reader.features.write.view


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.features.write.viewmodel.WriteTagViewModel
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/*
* this feature is used for writing tags manually by a user.
* will be deprecated or rarely used after all items tagged by RFID labels.
*
*/

@AndroidEntryPoint
class WriteTag : ComponentActivity() {

    val viewModel: WriteTagViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposableHost(viewModel, onBackPressed = { finish() }, { onBottomBarButtonClick() })
        }
        viewModel.init()
        exceptionHandler()

        this.onBackPressedDispatcher.addCallback {
            finish()
        }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (keyCode == 280 || keyCode == 139 || keyCode == 293) {
            if (event.repeatCount == 0) {
                viewModel.scanTrigger()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onBottomBarButtonClick() {

        if (viewModel.counterValue >= viewModel.counterMaxValue) {
            showLog(
                "مجوز رایت وجود ندارد یا به پایان رسیده است. برای دریافت مجوز با پشتیبانی تماس بگیرید.",
                viewModel.state
            )
        } else {
            Intent(this, FixTagErrors::class.java).apply {
                startActivity(this)
            }
        }
    }
}

@Composable
private fun ComposableHost(
    viewModel: WriteTagViewModel,
    onBackPressed: () -> Unit,
    onBottomBarButtonClick: () -> Unit,
) {
    Page(
        state = viewModel.state,
        popupState = viewModel.popupState,
        result = viewModel.result,
        resultColor = viewModel.resultColor,
        isWritingToTag = viewModel.loading,
        onBackPressed = onBackPressed,
        onBottomBarButtonClick = onBottomBarButtonClick,
    )
}


@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@Composable
private fun Page(
    state: SnackbarHostState,
    popupState: NotificationPopupHost,
    result: String,
    resultColor: Color,
    isWritingToTag: Boolean,
    onBackPressed: () -> Unit,
    onBottomBarButtonClick: () -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = { AppBarWithBack(onBackPressed = onBackPressed, title = "رایت") },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(
                            result = result,
                            isWritingToTag = isWritingToTag,
                            resultColor = resultColor,
                            popupState = popupState,
                        )
                    }
                },
                bottomBar = {
                    BottomBarButton(text = "اصلاح تگ های معیوب", onClick = onBottomBarButtonClick)
                },

                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
private fun Content(
    result: String,
    isWritingToTag: Boolean,
    resultColor: Color,
    popupState: NotificationPopupHost,
) {

    Column {

        if (isWritingToTag) {
            LoadingCircularProgressIndicator(isScanning = isWritingToTag)
        } else {

            NotificationPopUp(state = popupState)

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .border(
                        BorderStroke(1.dp, primaryLight),
                        shape = MaterialTheme.shapes.small
                    )
                    .background(
                        color = resultColor,
                        shape = MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Text(
                    text = result,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Page(
        state = SnackbarHostState(),
        popupState = NotificationPopupHost(),
        result = "this is test",
        resultColor = Color.White,
        isWritingToTag = false,
        onBottomBarButtonClick = {},
        onBackPressed = {}
    )
}
