package com.jeanwest.reader.features.banimode.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeanwest.reader.R
import com.jeanwest.reader.features.banimode.viewmodel.BanimodeReturnViewModel
import com.jeanwest.reader.view.AlertDialogWith2Button
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.models.BaniReturn
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main activity for the Banimode Return feature.
 * This activity displays a list of UI elements and handles user interactions.
 * It initializes the ViewModel, sets up the content, and manages back navigation.
 *
 *  Key responsibilities:
 *  - Initializes and injects the [BanimodeReturnViewModel] using Hilt.
 *  - Fetches the UI list data using the ViewModel upon creation.
 *  - Sets up the Compose UI using the `Page` composable.
 *  - Handles back button presses, allowing the user to navigate back.
 *  - Manages activity lifecycle events (onPause, onResume).
 *  - Implements a global exception handler to catch and handle uncaught exceptions.
 */
@AndroidEntryPoint
class BanimodeReturn : ComponentActivity() {

    val viewModel: BanimodeReturnViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        viewModel.getUiList()
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
    viewModel: BanimodeReturnViewModel,
    back: () -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        onBackPressed = { back() },
                        stringResource(R.string.banimodeReturn)
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(viewModel)
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Content(
    view: BanimodeReturnViewModel,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (view.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
            }
        } else {
            if (view.openAddDialog) {
                AlertDialogWith2Button("حواله مورد نظر نهایی شود؟", "بله", "خیر", {
                    view.openAddDialog = false
                }, {
                    view.finalConfirm(view.stockDraftId)
                    view.openAddDialog = false
                }) {
                    view.openAddDialog = false
                }
            }
            if (view.uiList.isEmpty()) {
                EmptyBox("هیچ حواله ی برگشتی وجود ندارد")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding()
                        .fillMaxSize()
                ) {
                    items(view.uiList.size) { i ->
                        BaniReturnItems(i, view.uiList, view)
                    }
                }
            }
        }
    }
}


@Composable
fun BaniReturnItems(i: Int, uiList: List<BaniReturn>, view: BanimodeReturnViewModel) {

    val topPadding = if (i == 0) 16.dp else 12.dp
    val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

    Column(
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            .shadow(elevation = 5.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = if (uiList[i].color == "#f0f050") {
                    Color.Yellow
                } else {
                    Color.Green
                },
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(120.dp)
            .testTag("items")

    ) {
        Row(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .weight(1.5f), Arrangement.SpaceEvenly
        ) {
            Text(
                text = uiList[i].address,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Right,
                color = Color.Black,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.Top)
                    .wrapContentHeight()
                    .padding(end = 10.dp, top = 10.dp)
                    .weight(4f)
            )
            Text(
                text = "ID: " + uiList[i].id,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .align(Alignment.Top)
                    .weight(1.7f)
                    .padding(top = 10.dp)
            )
        }
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 10.dp, bottom = 10.dp)
                .fillMaxWidth()
                .weight(1f), Arrangement.SpaceBetween
        ) {
            Text(
                text = "تاریخ: " + uiList[i].saleDate,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Right,
                modifier = Modifier.align(Alignment.Bottom)
            )
            Button(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(end = 10.dp),
                onClick = {
                    view.stockDraftId = uiList[i].id
                    view.openAddDialog = true
                }
            ) {
                Text(
                    text = "نهایی",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}