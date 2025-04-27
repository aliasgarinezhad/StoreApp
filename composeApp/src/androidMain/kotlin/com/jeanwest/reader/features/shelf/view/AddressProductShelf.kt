package com.jeanwest.reader.features.shelf.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.EmptyShelf
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.features.shelf.viewmodel.AddressProductShelfViewModel
import com.jeanwest.reader.models.StoreShelf
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 *  Activity responsible for displaying and managing the product shelf associated with a specific address.
 *  This activity uses a ViewModel ([AddressProductShelfViewModel]) to handle data and business logic,
 *  and a composable ([Page]) to render the user interface.
 *
 *  Key Features:
 *      - Displays products available at a given address.
 *      - Allows interaction with products (e.g., viewing details, adding to cart). (Functionality implemented in the associated ViewModel and composable).
 *      - Handles exceptions using a custom [ExceptionHandler].
 *      - Manages activity lifecycle events (onPause, onResume) to synchronize with ViewModel state.
 *
 *  Usage:
 *      This activity is typically launched with address information as intent extras (though not explicitly shown in this code snippet.  The specifics of how the address is passed are handled within the ViewModel and calling code).
 *      The [Page] composable function should handle the display and interaction with the product shelf data provided by the [AddressProductShelfViewModel].
 *
 *  Dependencies:
 *      - Dagger Hilt for dependency injection (ViewModel instantiation).
 *      - [AddressProductShelfViewModel] (ViewModel) for data management.
 *      - [Page] (Composable function) for UI rendering.
 *      - [ExceptionHandler] (Custom exception handler, implementation not shown).
 */
@AndroidEntryPoint
class AddressProductShelf : ComponentActivity() {

    val viewModel: AddressProductShelfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            Page(viewModel) {
                finish()
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
fun Page(viewModel: AddressProductShelfViewModel, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(onBackPressed, stringResource(R.string.AddressProductStore))
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
            )
        }
    }
}

@Composable
fun Content(viewModel: AddressProductShelfViewModel) {
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
                })
            EmptyShelf("بارکد کالا را اسکن یا وارد کنید")
        }
    }
}

@Composable
fun Content2(viewModel: AddressProductShelfViewModel) {
    Column {
        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else {
            if (viewModel.uiList.isEmpty()) {
                EmptyShelf("این کالا در هیچ قفسه ای وجود ندارد")
            } else {
                LazyColumn {
                    items(viewModel.uiList.size) { i ->
                        Items(
                            i,
                            viewModel.uiList
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Items(i: Int, uiList: MutableList<StoreShelf>) {

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
                color = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(90.dp)
            .testTag("items")

    ) {
        Row(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .weight(1f), Arrangement.SpaceBetween
        ) {
            Text(
                text = "شماره قفسه: ${uiList[i].shelfTitle}",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Right,
                color = Color.Black,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = "نوع قفسه: ${uiList[i].shelfTitleType}",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Right,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 10.dp, bottom = 10.dp)
                .fillMaxWidth()
                .weight(1f), Arrangement.SpaceBetween
        ) {
            Text(
                text = "توضیحات: ${uiList[i].shelfDes}",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Right,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}