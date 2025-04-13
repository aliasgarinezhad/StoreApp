package com.jeanwest.reader.features.stockDraft.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.gmail.hamedvakhide.compose_jalali_datepicker.JalaliDatePickerDialog
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.Item7
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.models.StockDraftHistory
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


@AndroidEntryPoint
class StockDraftsHistoryViewModel @Inject constructor(
    @set:Inject
    var state: SnackbarHostState,
    @ApplicationContext var context: Context,
) : ComponentActivity() {
    var back: () -> Unit = {}
    var title: String = "تاریخچه حواله ها"
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<StockDraftHistory>()
    var uiListOnClick: (product: StockDraftHistory) -> Unit = { _ -> }
    var warehouseCodeToString: (code: String) -> String = { code -> code }
    var dateConverter: (date: String) -> String = { date -> date }
    var sourceWarehouseFilterValue by mutableStateOf("همه مبدا ها")
    var destinationWarehouseFilterValue by mutableStateOf("همه مقصد ها")
    var stockDraftStateFilterValue by mutableStateOf("وضعیت حواله")
    var allDestinationWarehouseFilterValues = mutableStateListOf<String>()
    var allSourceWarehouseFilterValues = mutableStateListOf<String>()
    var allStateFilterValues = mutableStateListOf(
        "همه",
        "نهایی",
        "ابطال شده",
        "حواله های باز"
    )
    var sourceFilterOnClick: (warehouse: String) -> Unit = { _ -> }
    var destinationFilterOnClick: (warehouse: String) -> Unit = { _ -> }
    var stateFilterOnClick: (state: String) -> Unit = { _ -> }
    var convertToISO8601: (day: Int, month: Int, year: Int) -> String =
        { day, month, year -> day.toString(); month.toString(); year.toString() }
    var callApi: () -> Unit = {}
    var source by mutableStateOf<Int?>(null)
    var des by mutableStateOf<Int?>(null)
    var stDate by mutableStateOf<String?>(null)
    var endDate by mutableStateOf<String?>(null)
    var showStDate by mutableStateOf("از تاریخ:")
    var showEndDate by mutableStateOf("تا تاریخ:")
    var stateId by mutableStateOf<Int?>(null)
}

@OptIn(ExperimentalCoilApi::class)
@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@ExperimentalFoundationApi
@Composable
fun Page(viewModel: StockDraftsHistoryViewModel) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        onBackPressed = { viewModel.back() },
                        viewModel.title
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(viewModel = viewModel)
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = {},
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@Composable
fun Content(viewModel: StockDraftsHistoryViewModel) {

    Column {

        if (viewModel.loading) {
            LoadingCircularProgressIndicator(
                isDataLoading = viewModel.loading
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    FilterDropDownList(
                        text = {
                            Text(
                                text = viewModel.sourceWarehouseFilterValue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        values = viewModel.allSourceWarehouseFilterValues
                    ) {
                        viewModel.sourceFilterOnClick(it)
                    }

                    FilterDropDownList(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                        text = {
                            Text(
                                text = viewModel.destinationWarehouseFilterValue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        values = viewModel.allDestinationWarehouseFilterValues
                    ) {
                        viewModel.destinationFilterOnClick(it)
                    }

                }

                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.CenterHorizontally)
                        .background(Color.White),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 15.dp)
                            .background(Color.Transparent),
                    ) {
                        val openDialogStDate = remember { mutableStateOf(false) }
                        val openDialogEndDate = remember { mutableStateOf(false) }
                        Button(
                            onClick = { openDialogStDate.value = true },
                            modifier = Modifier
                                .background(Color.White)
                                .fillMaxWidth(),
                        ) {
                            Text(
                                text = viewModel.showStDate,
                                color = Color.Black
                            )
                        }
                        JalaliDatePickerDialog(
                            openDialog = openDialogStDate,
                            onSelectDay = {
                            },
                            onConfirm = { it1 ->
                                Log.e(
                                    "st date:",
                                    viewModel.convertToISO8601(it1.day, it1.month, it1.year)
                                )
                                viewModel.stDate =
                                    viewModel.convertToISO8601(it1.day, it1.month, it1.year)
                                viewModel.showStDate = "${it1.day} ${it1.monthString} ${it1.year}"
                                viewModel.callApi()

                            },
                            backgroundColor = Color.White
                        )

                        Button(
                            onClick = { openDialogEndDate.value = true },
                            modifier = Modifier
                                .background(Color.White)
                                .fillMaxWidth(),
                        ) {
                            Text(
                                text = viewModel.showEndDate,
                                color = Color.Black
                            )
                        }
                        JalaliDatePickerDialog(
                            openDialog = openDialogEndDate,
                            onSelectDay = {
                            },
                            onConfirm = {
                                viewModel.endDate =
                                    viewModel.convertToISO8601(it.day, it.month, it.year)
                                viewModel.showEndDate = "${it.day} ${it.monthString} ${it.year}"
                                viewModel.callApi()
                            },
                            backgroundColor = Color.White
                        )
                    }
                    FilterDropDownList(
                        modifier = Modifier
                            .padding(15.dp),
                        text = {
                            Text(
                                text = viewModel.stockDraftStateFilterValue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        values = viewModel.allStateFilterValues
                    ) {
                        viewModel.stateFilterOnClick(it)
                    }
                }

                LazyColumn(modifier = Modifier.padding(top = 4.dp)) {

                    items(viewModel.uiList.size) { i ->
                        Item7(
                            clickable = true,
                            enableBottomSpace = i == viewModel.uiList.size - 1,
                            text1 = "حواله: " + viewModel.uiList[i].stockDraftID,
                            text2 = "از: " + viewModel.warehouseCodeToString(viewModel.uiList[i].fromWareHouseID),
                            text4 = "به: " + viewModel.warehouseCodeToString(viewModel.uiList[i].toWareHouseID),
                            text3 = "وضعیت: " + viewModel.uiList[i].statusTitle,
                            text7 = "تاریخ ایجاد: " + viewModel.dateConverter(viewModel.uiList[i].createDate),
                            text5 = "تاریخ ارسال: " + viewModel.dateConverter(viewModel.uiList[i].sendDate),
                            text6 = "تاریخ اپدیت: " + viewModel.dateConverter(viewModel.uiList[i].updateDate)
                        ) {
                            viewModel.uiListOnClick(viewModel.uiList[i])
                        }
                    }
                }
            }
        }
    }
}
