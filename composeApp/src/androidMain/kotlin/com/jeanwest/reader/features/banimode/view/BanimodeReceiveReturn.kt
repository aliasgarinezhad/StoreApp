package com.jeanwest.reader.features.banimode.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.jeanwest.reader.R
import com.jeanwest.reader.features.banimode.viewmodel.BanimodeReceiveReturnViewModel
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.NotificationPopUp
import com.jeanwest.reader.view.ScanFilterDropDownList
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.view.errorContainerLight
import com.jeanwest.reader.view.errorLight
import com.jeanwest.reader.view.primaryLight
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 *  `BanimodeReceiveReturn` is an Activity designed to handle the return process within the Banimode application.
 *  It displays a user interface (for managing returns) and interacts with a ViewModel to handle data and business logic.
 *
 *  Key Features:
 *  - **UI Composition with Jetpack Compose:**  The activity uses Jetpack Compose (`setContent`) to render its user interface. The specific UI is defined in the `Page` composable function.
 *  - **ViewModel Integration:**  It leverages a `BanimodeReceiveReturnViewModel` to manage the UI state and handle user interactions, likely related to return processing.  The ViewModel is lazily initialized using `by viewModels()`.
 *  - **Data Persistence (Possible):** The activity interacts with the ViewModel to potentially load data from persistent storage (`viewModel.loadMemory()` and `viewModel.checkLoadMemoryData()`). The exact implementation is within the ViewModel.
 *  - **Lifecycle Management:** It implements `onPause` and `onResume` to manage activity lifecycle events, potentially pausing/resuming operations within the ViewModel (e.g., network requests or resource usage).
 *  - **Back Navigation:** The `back()` function handles the back button press, terminating the activity using `finish()`. It uses `onBackPressedDispatcher.addCallback` to override the default back button behavior and ensure consistent back navigation.
 *  - **Exception Handling:**  The `exceptionHandler()` sets a default uncaught exception handler to gracefully handle crashes. This likely directs exceptions to a central error reporting mechanism, but the specific `ExceptionHandler` implementation is not shown in this snippet.
 *  - **Hilt Dependency Injection:** The `@AndroidEntryPoint` annotation enables Hilt for dependency injection, providing the ViewModel and potentially other dependencies.
 *
 *  Usage:
 *  This activity would typically be launched by another part of the Banimode application when a return needs to be processed or displayed. The activity's UI would then guide the user through the necessary steps, while the ViewModel handles data updates and interactions with the backend (if applicable).
 *
 *  Key */
@AndroidEntryPoint
class BanimodeReceiveReturn : ComponentActivity() {

    val viewModel: BanimodeReceiveReturnViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        viewModel.loadMemory()
        viewModel.checkLoadMemoryData()
        setContent {
            Page(viewModel) {
                back()
            }
        }
        this.onBackPressedDispatcher.addCallback(this) {
            back()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()

    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    private fun back() {
        finish()
    }
}

@ExperimentalFoundationApi
@Composable
fun Page(
    viewModel: BanimodeReceiveReturnViewModel,
    back: () -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        onBackPressed = { back() },
                        stringResource(R.string.banimodeReceiveReturn)
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(viewModel)
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = {
                    if (viewModel.step == 1
                    ) {
                        BottomBar(viewModel)
                    }
                }
            )
        }
    }
}

@Composable
fun BottomBar(view: BanimodeReceiveReturnViewModel) {

    BottomAppBar(
        modifier = Modifier.wrapContentHeight()
    ) {

        Column {

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScanFilterDropDownList(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    filterValue = view.scanFilter
                ) { value ->
                    view.changeFilterValue(value)
                }
                Button(onClick = {
                    view.popupState.showPopupWith2Button(
                        message = "فرآیند نهایی شود؟",
                        okButtonTitle = "بله",
                        cancelButtonTitle = "خیر",
                        onOkClick = {
                            view.finalizeBaniStocks()
                        },

                        )
                }) {
                    Text(text = "تایید نهایی")
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Content(
    view: BanimodeReceiveReturnViewModel,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (view.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
            }
        } else {

            NotificationPopUp(view.popupState)

            if (view.step == 0) {
                SimpleTextField(
                    value = view.stockDraftId,
                    hint = "شماره حواله مرجوعی",
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                            top = 12.dp
                        )
                        .fillMaxWidth(),
                    onValueChange = {
                        view.stockDraftId = it
                    },
                    onDone = {
                        if (view.stockDraftId != "" && view.stockDraftId.isDigitsOnly()) {
                            view.getStockDraftDetails()
                        } else {
                            showLog("حواله وارد شده نامعتبر است.", state = view.state)
                        }
                    },
                    keyboardType = KeyboardType.Number
                )
                EmptyBox("حواله مرجوعی را اسکن یا در کادر بالا وارد کنید")
            } else if (view.step == 1) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, primaryLight), shape = MaterialTheme.shapes.small
                        )
                        .background(
                            MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.small
                        ),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Text(
                        text = "اسکن: ${view.validatedNumber}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                    Text(
                        text = "کسری: ${view.shortageNumber}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 10.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                    Text(
                        text = "اضافی: ${view.additionalNumber}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                }

                LazyColumn(
                    modifier = Modifier.padding(bottom = 56.dp)
                ) {
                    items(view.filteredUiList.size) { i ->
                        BaniRecieveLazyColumnItem(i, view)
                    }
                }
            }
        }
    }
}


@Composable
fun BaniRecieveLazyColumnItem(i: Int, view: BanimodeReceiveReturnViewModel) {

    val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

    Box {
        Item(
            i, view.filteredUiList, false,
            text3 = "درخواستی: " + view.filteredUiList[i].draftNumber,
            text4 = view.filteredUiList[i].let { it.conflictType + ": " + it.conflictNumber }
        )

        if (view.filteredUiList[i].conflictType == "اضافی") {

            Box(
                modifier = Modifier
                    .padding(top = topPaddingClearButton, end = 8.dp)
                    .background(
                        shape = RoundedCornerShape(36.dp), color = errorContainerLight
                    )
                    .size(30.dp)
                    .align(TopEnd)
                    .testTag("clear")
                    .clickable {
                        view.clearItem(view.filteredUiList[i])
                    }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_clear_24),
                    contentDescription = "",
                    tint = errorLight,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp)
                )
            }
        }
    }
}