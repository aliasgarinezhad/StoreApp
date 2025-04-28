package com.jeanwest.reader.features.shelf.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.EmptyShelf
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.view.errorContainerLight
import com.jeanwest.reader.view.errorLight
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.features.shelf.viewmodel.ShelfEnterStoreViewModel
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 *  [ShelfEnterStore] is an Activity that handles the process of entering products into a store's inventory shelf.
 *  It utilizes a [ShelfEnterStoreViewModel] to manage the UI state and data related to product entry.
 *  The activity is designed to handle exceptions gracefully and manages its lifecycle events (pause/resume) to
 *  interact with the ViewModel accordingly.
 *
 *  Key functionalities:
 *  - **Product Entry:** Allows users to add products to a virtual or physical shelf within a store.  The specific UI
 *      and interaction details are handled by the `Page` composable, not directly within this activity class.
 *  - **Data Management:**  Leverages a [ShelfEnterStoreViewModel] to store and update information about products being
 *      entered, including a list of UI representations (`uiListProduct`), SKUs (`skuList`), and barcodes (`barcodes`).
 *  - **UI State Control:** Uses an integer `uiState` within the ViewModel to manage the current state of the product
 *      entry process.  Specifically:
 *      - `uiState == 1`: Indicates completion of the product entry, triggering a cleanup of product data
 *        and resetting the state to `uiState = 0`.
 *      - `uiState != 1`:  Represents an incomplete state, causing the activity to finish (likely returning to a
 *        previous screen).
 *  - **Exception Handling:** Implements a custom [ExceptionHandler] to handle uncaught exceptions and prevent app
 *      crashes.  The exact behavior of the exception handler (logging, reporting, etc.) would be defined within
 *      the `ExceptionHandler` class.
 *  - **Lifecycle Management:** Overrides `onPause` and `onResume` to call corresponding methods in the
 *      [ShelfEnterStoreViewModel], allowing it to manage resources or operations appropriately based on the
 *      activity's visibility.
 *
 *  Note: The core UI for product entry is implemented by the `Page` composable function, which is invoked within
 *  the `setContent` block.  The details of the UI (e.g., fields for entering product details */
@AndroidEntryPoint
class ShelfEnterStore : ComponentActivity() {

    val viewModel: ShelfEnterStoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            Page(viewModel) {
                if (viewModel.uiState == 1) {
                    viewModel.uiListProduct.clear()
                    viewModel.skuList.clear()
                    viewModel.barcodes.clear()
                    viewModel.uiState = 0
                } else {
                    finish()
                }
            }
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

}

@Composable
fun Page(viewModel: ShelfEnterStoreViewModel, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(onBackPressed, stringResource(R.string.EnterShelfStore))
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        when (viewModel.uiState) {
                            0 -> Content(viewModel)
                            1 -> Content2(viewModel)
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = {
                    if (viewModel.uiListProduct.isNotEmpty()) {
                        BottomBarButton("ورود به قفسه") {
                            viewModel.addProductToShelf()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun Content(viewModel: ShelfEnterStoreViewModel) {
    Column {
        if (viewModel.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(false, viewModel.loading)
            }
        } else {
            SimpleTextField(
                value = viewModel.shelfCode,
                hint = "شماره قفسه",
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 12.dp
                    )
                    .fillMaxWidth(),
                onValueChange = {
                    viewModel.shelfCode = it
                },
                onDone = {
                    if (viewModel.shelfCode.isNotBlank()) {
                        viewModel.getShelProducts(viewModel.shelfCode)
                    }
                })
            EmptyShelf("بارکد قفسه را اسکن یا وارد کنید")
        }
    }
}

@Composable
fun Content2(viewModel: ShelfEnterStoreViewModel) {
    Column {
        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else {
            SimpleTextField(
                value = viewModel.productBarcode,
                hint = "بارکد کالا",
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 12.dp
                    )
                    .fillMaxWidth(),
                onValueChange = {
                    viewModel.productBarcode = it
                },
                onDone = {
                    if (viewModel.shelfCode.isNotBlank()) {
                        viewModel.getProductDetails(viewModel.productBarcode)
                    }
                })
            if (viewModel.uiListProduct.isEmpty()) {
                EmptyShelf("بارکد کالا را اسکن یا وارد کنید")
            } else {
                LazyColumn {
                    items(viewModel.uiListProduct.size) { i ->
                        val topPaddingClearButton = if (i == 0) 8.dp else 4.dp
                        Box {
                            Item(
                                i,
                                viewModel.uiListProduct,
                                text3 = "رنگ: " + viewModel.uiListProduct[i].color,
                                text4 = "سایز: " + viewModel.uiListProduct[i].size
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = topPaddingClearButton, end = 8.dp)
                                    .background(
                                        shape = RoundedCornerShape(36.dp),
                                        color = errorContainerLight
                                    )
                                    .size(30.dp)
                                    .align(Alignment.TopEnd)
                                    .testTag("clear")
                                    .clickable {
                                        viewModel.clear(viewModel.uiListProduct[i])
                                    }
                            ) {
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
            }
        }
    }
}