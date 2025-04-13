@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.shelf.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeanwest.reader.R
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.EmptyShelf
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.shelf.viewmodel.CreateShelfStoreViewModel
import com.jeanwest.reader.models.StoreShelf
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateShelfStore : ComponentActivity() {

    val viewModel: CreateShelfStoreViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
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

@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@ExperimentalFoundationApi
@Composable
fun Page(
    viewModel: CreateShelfStoreViewModel,
    back: () -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = { AppBarWithBack(onBackPressed = { back() }, viewModel.title) },
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
    view: CreateShelfStoreViewModel,
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
                BasicAlertDialog(
                    onDismissRequest = {
                        view.openAddDialog = false
                    },
                    content = {
                        Column(
                            modifier = Modifier
                                .shadow(6.dp, Shapes.medium)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = MaterialTheme.shapes.large
                                )
                                .fillMaxWidth(),
                        ) {

                            Row {
                                SimpleTextField(
                                    value = view.shelfDes,
                                    hint = "توضیحات(اختیاری)",
                                    keyboardType = KeyboardType.Text,
                                    modifier = Modifier
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            bottom = 12.dp,
                                            top = 12.dp
                                        )
                                        .fillMaxWidth(),
                                    onValueChange = {
                                        view.shelfDes = it
                                    },
                                    onDone = {

                                    })
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                Arrangement.SpaceEvenly
                            ) {

                                FilterDropDownList(
                                    modifier = Modifier,
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_shelf),
                                            contentDescription = "",
                                            tint = primaryLight,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .align(Alignment.CenterVertically)
                                                .padding(start = 6.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = view.shelfTypeText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .padding(start = 6.dp)
                                        )
                                    },
                                    onClick = {
                                        view.shelfTypeText = it
                                    },
                                    values = view.shelfTypes.keys.toMutableList()
                                )
                                Button(
                                    onClick = {
                                        view.createShelf()
                                    },
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .testTag("alertBtn")
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = "ایجاد قفسه",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                )
            }
            Log.e("uilist isEmpty:", view.uiList.toList().toString())
            if (view.uiList.isEmpty()) {
                Box(modifier = Modifier.padding(20.dp)) {
                    EmptyShelf("هیچ قفسه ای در این دپارتمان وجود ندارد")
                    ExtendedFab(
                        modifier = Modifier
                            .align(Alignment.BottomEnd),
                        view
                    )
                }
            } else {
                SimpleTextField(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    hint = "جستجو",
                    onValueChange = { filter ->
                        view.filterValue = filter
                        view.uiList.filter {
                            it.shelfDes.contains(filter)
                        }
                        view.uiList.filter {
                            it.shelfTitle.contains(filter)
                        }
                    },
                    value = view.filterValue
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    val filteredStockDraftUiList = if (view.filterValue == "") {
                        view.uiList
                    } else {
                        view.uiList.filter {
                            it.shelfTitle.contains(view.filterValue)
                                    ||
                                    it.shelfDes.contains(view.filterValue)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .padding()
                            .fillMaxSize()
                    ) {
                        items(filteredStockDraftUiList.size) { i ->
                            CreateShelfItems(i, filteredStockDraftUiList, view)
                        }
                    }
                    ExtendedFab(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp),
                        view
                    )
                }
            }
        }
    }
}

@Composable
fun ExtendedFab(modifier: Modifier, view: CreateShelfStoreViewModel) {
    ExtendedFloatingActionButton(
        onClick = {
            view.openAddDialog = !view.openAddDialog
        },
        modifier = modifier,
        containerColor = primaryLight,
        contentColor = Color.White,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White
            )
        },
        text = {
            Text(
                "ایجاد قفسه",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
    )
}

@Composable
fun CreateShelfItems(i: Int, uiList: List<StoreShelf>, view: CreateShelfStoreViewModel) {

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
            Button(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(end = 10.dp),
                onClick = {
                    view.printShelf(uiList[i].shelfTitle)
                }
            ) {
                Text(
                    text = "پرینت",
                    style = Typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}