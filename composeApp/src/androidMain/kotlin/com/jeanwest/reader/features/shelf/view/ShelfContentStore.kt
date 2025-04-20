package com.jeanwest.reader.features.shelf.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.jeanwest.reader.R
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.primaryContainerLight
import com.jeanwest.reader.features.shared.secondaryLight
import com.jeanwest.reader.features.shared.warningColor
import com.jeanwest.reader.features.shelf.viewmodel.ShelfContentStoreViewModel
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity responsible for displaying and managing the Shelf Content Store.
 *
 * This activity uses Jetpack Compose for its UI and interacts with a [ShelfContentStoreViewModel]
 * to handle data and business logic.  It allows the user to navigate and manage the contents
 * of a shelf, likely within a warehouse or storage facility context.
 *
 * Key features:
 * - Displays a page with content managed by [ShelfContentStoreViewModel].
 * - Handles back navigation, allowing the user to move between different states or exit the activity.
 * - Includes error handling for uncaught exceptions.
 * - Manages activity lifecycle events (pause, resume) and informs the view model.
 * - Listens for hardware back button presses and triggers back navigation.
 *
 *  @ExperimentalFoundationApi is used because it contains new features that are still in development and might change in future versions of Compose.
 */
@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class ShelfContentStore : ComponentActivity() {

    val viewModel: ShelfContentStoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            Page(viewModel) {
                back()
            }
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
        if (viewModel.pageState != 0) {
            viewModel.uiList.clear()
            viewModel.cageNumber = ""
            viewModel.pageState = 0
        } else finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 4) {
                back()
            }
        }
        return true
    }
}

@ExperimentalFoundationApi
@Composable
fun Page(viewModel: ShelfContentStoreViewModel, back: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        { back() },
                        stringResource(id = R.string.shelf_content)
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        when (viewModel.pageState) {
                            0 -> ScanPage(viewModel)
                            1 -> ProductContent(viewModel)
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) }
            )
        }
    }
}


@ExperimentalFoundationApi
@Composable
fun ProductContent(viewModel: ShelfContentStoreViewModel) {
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
        }
        NotificationPopUp(viewModel.popupState)
        if (viewModel.uiList.isEmpty()) {
            EmptyBox(text = "کالایی در این قفسه وجود ندارد")
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 16.dp, start = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = "مجموع: " + (viewModel.uiList.size).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
            LazyColumn {
                items(viewModel.uiList.size) { i ->
                    AdditionalItems(i, viewModel)
                }
            }
        }
    }
}

@Composable
fun AdditionalItems(i: Int, viewModel: ShelfContentStoreViewModel) {
    Box {
        ItemShelfStore(
            i, viewModel.uiList, false,
            text3 = "موجودی: ",
            text4 = "سایز: ",
            enableSign = true
        )
    }
}

@Composable
fun ScanPage(viewModel: ShelfContentStoreViewModel) {
    ScanOrTypeNumberPage(
        loading = viewModel.loading,
        onClick = {
            viewModel.getShelfProducts()
        },
        value = viewModel.cageNumber,
        onValueChange = { viewModel.cageNumber = it },
        item = "شماره قفسه"
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemShelfStore(
    i: Int,
    uiList: List<Product> = mutableListOf(),
    clickable: Boolean = false,
    text3: String,
    text4: String,
    enableSign: Boolean = false,
    signNumber: Int = 0,
    colorFull: Boolean = false,
    enableWarehouseNumberCheck: Boolean = false,
    text1: String = "",
    text2: String = "",
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {

    val topPadding = if (i == 0) 16.dp else 12.dp
    val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            .shadow(elevation = 5.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = if (uiList[i].scannedNumber > uiList[i].wareHouseNumber && enableWarehouseNumberCheck) {
                    errorLight
                } else if (colorFull) {
                    secondaryLight
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onLongClick = { onLongClick() },
                onClick = { onClick() })
    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(uiList[i].imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .shadow(0.dp, shape = Shapes.large)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = Shapes.large
                    )
                    .border(
                        BorderStroke(2.dp, color = primaryContainerLight),
                        shape = Shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            if (uiList[i].requestedNumber > 0 || (enableSign && signNumber > 0)) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                        .testTag("sign")
                ) {
                    Text(
                        text = if (enableSign) signNumber.toString() else uiList[i].requestedNumber.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") uiList[i].KBarCode.split("-").take(2)
                        .joinToString("-") else text1,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") uiList[i].name else text2,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Right,
                )
            }

//            Column(
//                modifier = Modifier
//                    .weight(1F)
//                    .fillMaxHeight()
//                    .padding(top = 16.dp, bottom = 16.dp)
//                    .wrapContentWidth()
//                    .background(
//                        color = primaryContainerLight,
//                        shape = Shapes.large
//                    ),
//
//                verticalArrangement = Arrangement.SpaceEvenly
//            ) {
//                Text(
//                    text = text3,
//                    style = MaterialTheme.typography.labelMedium,
//                    textAlign = TextAlign.Right,
//                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
//                )
//                Divider(
//                    color = primaryLight,
//                    thickness = 1.dp,
//                    modifier = Modifier
//                        .padding(horizontal = 12.dp, vertical = 2.dp)
//                        .width(66.dp)
//                )
//                Text(
//                    text = text4,
//                    style = MaterialTheme.typography.labelMedium,
//                    textAlign = TextAlign.Right,
//                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
//                )
//            }
        }
    }
}