@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.inventory.view

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.features.inventory.viewmodel.InventoryViewModel
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.models.Product
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
@ExperimentalFoundationApi
class Inventory : ComponentActivity() {

    val viewModel by viewModels<InventoryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Page(
                back = { viewModel.back() },
                onFinishInventoryButtonClick = { viewModel.onInventoryFinishButtonClick() },
                loading = viewModel.loading,
                scanning = viewModel.rf.scanning,
                sexTileFilterValues = viewModel.sexTileFilterValues,
                sexTileFilterValue = viewModel.sexTileFilterValue,
                scanFilter = viewModel.scanFilter,
                state = viewModel.state,
                closeActivity = { finish() },
                onScanFilterValueChanged = { viewModel.onFilterValueChanged(it) },
                onSexFilterValueChanged = { viewModel.onSexFilterValueChanged(it) },
                onProductLongClick = { viewModel.onProductLongClick(it) },
                onProductClick = { viewModel.onProductClick(it) },
                uiList = viewModel.uiList,
                signedKBarCode = viewModel.signedKBarCode,
                isInShortageAdditionalPage = viewModel.isInShortageAdditionalPage,
                startInventory = { viewModel.startInventory() },
                openSearchActivity = { openSearchActivity(it) },
                popupHost = viewModel.popupHost,
                inventoryProgress = viewModel.inventoryProgress,
                shortagesNumber = viewModel.shortagesNumber,
                additionalNumber = viewModel.additionalNumber,
                isInventoryStarted = viewModel.isInventoryStarted,
                allInventoryNumber = viewModel.allInventoryNumber,
                confirmedNumber = viewModel.confirmedNumber,
                onInventoryConflictButtonClick = { viewModel.onInventoryConflictButtonClick() }
            )
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {

            if (keyCode == 280 || keyCode == 293) {
                viewModel.scanTrigger()
            } else if (keyCode == 4) {
                viewModel.back()
            }
        }
        return true
    }

    fun openSearchActivity(product: Product) {
        Intent(
            this@Inventory, SearchProduct::class.java
        ).apply {
            this.putExtra(
                "product", Gson().toJson(product).toString()
            )
            startActivity(this)
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@ExperimentalFoundationApi
@Composable
private fun Page(
    isInShortageAdditionalPage: Boolean,
    state: SnackbarHostState,
    loading: Boolean,
    scanning: Boolean,
    shortagesNumber: Int,
    additionalNumber: Int,
    scanFilter: String,
    sexTileFilterValue: String,
    sexTileFilterValues: List<String>,
    onSexFilterValueChanged: (value: String) -> Unit,
    onScanFilterValueChanged: (newValue: String) -> Unit,
    uiList: List<Product>,
    onProductClick: (productIndexInUiList: Int) -> Unit,
    signedKBarCode: List<String>,
    onProductLongClick: (productIndexInUiList: Int) -> Unit,
    isInventoryStarted: Boolean,
    startInventory: () -> Unit,
    back: () -> Unit,
    confirmedNumber: Int,
    allInventoryNumber: Int,
    inventoryProgress: Float,
    onFinishInventoryButtonClick: () -> Unit,
    popupHost: NotificationPopupHost,
    closeActivity: () -> Unit,
    openSearchActivity: (product: Product) -> Unit,
    onInventoryConflictButtonClick: () -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    if (isInShortageAdditionalPage) ListAppBar(
                        onBack = back
                    ) else AppBar(back = {
                        back()
                        closeActivity()
                    })
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        if (isInShortageAdditionalPage) ListContent(
                            loading = loading,
                            scanning = scanning,
                            onProductClick = onProductClick,
                            onScanFilterValueChanged = onScanFilterValueChanged,
                            onProductLongClick = onProductLongClick,
                            onSexFilterValueChanged = onSexFilterValueChanged,
                            additionalNumber = additionalNumber,
                            shortagesNumber = shortagesNumber,
                            scanFilter = scanFilter,
                            sexTileFilterValue = sexTileFilterValue,
                            sexTileFilterValues = sexTileFilterValues,
                            uiList = uiList,
                            signedKBarCode = signedKBarCode,
                            popupHost = popupHost,
                            openSearchActivity = openSearchActivity
                        ) else Content(
                            inventoryProgress = inventoryProgress,
                            confirmedNumber = confirmedNumber,
                            allInventoryNumber = allInventoryNumber,
                            scanning = scanning,
                            loading = loading
                        )
                    }
                },
                bottomBar = {
                    BottomBar(
                        loading = loading,
                        isInShortageAdditionalPage = isInShortageAdditionalPage,
                        isInventoryStarted = isInventoryStarted,
                        startInventory = startInventory,
                        onInventoryConflictButtonClick = onInventoryConflictButtonClick,
                        onFinishInventoryButtonClick = onFinishInventoryButtonClick
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
private fun AppBar(
    back: () -> Unit,
) {

    TopAppBar(

        navigationIcon = {
            IconButton(modifier = Modifier.testTag("back"), onClick = { back() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                    contentDescription = ""
                )
            }
        },

        title = {
            Text(
                text = stringResource(id = R.string.inventoryText),
                modifier = Modifier
                    .padding(end = 50.dp)
                    .fillMaxSize()
                    .wrapContentSize(),
                textAlign = TextAlign.Center,
            )
        })
}

@ExperimentalFoundationApi
@Composable
private fun Content(
    scanning: Boolean,
    loading: Boolean,
    inventoryProgress: Float,
    confirmedNumber: Int,
    allInventoryNumber: Int,
) {

    Box(
        modifier = Modifier
            .padding(bottom = 150.dp)
            .fillMaxSize()
    ) {

        if (scanning || loading) {
            LoadingCircularProgressIndicator(scanning, loading)
        }

        if (!scanning && !loading) {

            Box(
                modifier = Modifier
                    .shadow(1.dp, shape = Shapes.small)
                    .size(200.dp)
                    .align(
                        Alignment.Center
                    )
                    .background(color = Color.White, shape = Shapes.medium),
            ) {

                CircularProgressIndicator(
                    inventoryProgress, modifier = Modifier
                        .size(150.dp)
                        .align(
                            Alignment.Center
                        ), primaryLight, 8.dp
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = String.format(
                        locale = Locale.US,
                        "%.2f",
                        inventoryProgress * 100
                    ) + "%\n" + confirmedNumber + "/" + allInventoryNumber,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(
                            Alignment.Center
                        ),
                    textAlign = TextAlign.Center,
                    style = Typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    loading: Boolean,
    isInShortageAdditionalPage: Boolean,
    isInventoryStarted: Boolean,
    startInventory: () -> Unit,
    onInventoryConflictButtonClick: () -> Unit,
    onFinishInventoryButtonClick: () -> Unit,
) {

    if (!loading) {

        if (isInShortageAdditionalPage && isInventoryStarted) {
            BottomBarButton(text = "پایان انبارگردانی", onClick = onFinishInventoryButtonClick)
        } else if (!isInShortageAdditionalPage) {
            if (isInventoryStarted) {
                BottomBarButton(text = "مغایرت ها") {
                    onInventoryConflictButtonClick()
                    //isInShortageAdditionalPage = true
                }
            } else {
                BottomBarButton("شروع انبارگردانی") {
                    startInventory()
                }
            }
        }
    }
}

@Composable
fun ListAppBar(
    onBack: () -> Unit,
) {

    TopAppBar(

        navigationIcon = {
            IconButton(
                modifier = Modifier.testTag("back"),
                onClick = onBack
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                    contentDescription = ""
                )
            }
        },

        title = {
            Text(
                text = stringResource(id = R.string.inventoryText),
                modifier = Modifier
                    .padding(end = 50.dp)
                    .fillMaxSize()
                    .wrapContentSize(),
                textAlign = TextAlign.Center,
            )
        })
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@Composable
private fun ListContent(
    loading: Boolean,
    scanning: Boolean,
    shortagesNumber: Int,
    additionalNumber: Int,
    scanFilter: String,
    sexTileFilterValue: String,
    sexTileFilterValues: List<String>,
    onSexFilterValueChanged: (value: String) -> Unit,
    onScanFilterValueChanged: (newValue: String) -> Unit,
    uiList: List<Product>,
    onProductClick: (productIndexInUiList: Int) -> Unit,
    signedKBarCode: List<String>,
    onProductLongClick: (productIndexInUiList: Int) -> Unit,
    popupHost: NotificationPopupHost,
    openSearchActivity: (product: Product) -> Unit,
) {

    Column(Modifier.fillMaxSize()) {

        if (loading || scanning) {
            LoadingCircularProgressIndicator(
                scanning, loading
            )
        } else {

            NotificationPopUp(popupHost)

            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
            ) {

                Text(
                    text = "کسری: $shortagesNumber",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp)
                        .weight(1F)
                )

                Text(
                    text = "اضافی: $additionalNumber",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp)
                        .weight(1F)
                )
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1F)
                ) {
                    FilterDropDownList(
                        icon = { },
                        text = {
                            Text(
                                text = scanFilter,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        values = mutableListOf("اضافی", "کسری"),
                        onClick = onScanFilterValueChanged
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
            ) {

                Row(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1F)
                ) {
                    FilterDropDownList(
                        icon = { }, text = {
                        Text(
                            text = sexTileFilterValue,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp)
                        )
                    }, values = sexTileFilterValues,
                        onClick = {
                            onSexFilterValueChanged(it)
                        })
                }
            }

            LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {

                items(uiList.size) { i ->

                    Item(
                        i,
                        uiList,
                        text3 = "موجودی: " + uiList[i].inventoryNumber,
                        text4 = uiList[i].inventoryConflictType + ":" + " " + uiList[i].inventoryConflictAbs,
                        clickable = true,
                        onClick = {
                            onProductClick(i)
                            openSearchActivity(uiList[i])
                        },
                        colorFull = uiList[i].KBarCode in signedKBarCode,
                        onLongClick = { onProductLongClick(i) }
                    )
                }
            }
        }
    }
}