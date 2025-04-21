package com.jeanwest.reader.features.main.mainPage.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.mainTitle
import com.jeanwest.reader.data.remote.IotHub
import com.jeanwest.reader.features.main.utility.requestPermissions
import com.jeanwest.reader.features.main.mainPage.viewModel.MainViewModel
import com.jeanwest.reader.features.main.view.DeviceRegister
import com.jeanwest.reader.features.shared.AlertDialogWith2ButtonAndAppVersion
import com.jeanwest.reader.features.shared.AlertDialogWithHeadlineMediumButton
import com.jeanwest.reader.features.shared.AppBarWithNavigationButton
import com.jeanwest.reader.features.shared.BigButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.ExpandableCard
import com.jeanwest.reader.features.shared.FilterDropDownListWithSearch
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.OpenActivityButton
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.write.view.WriteTag
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The main activity of the application.
 *
 * This activity is the entry point for the app and handles the overall app lifecycle.
 * It initializes the UI using Jetpack Compose, requests necessary permissions, and manages
 * interactions with the [MainViewModel].  It also handles back press events.
 *
 * The activity is annotated with `@AndroidEntryPoint` for dependency injection using Hilt.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestPermissions(this)

        setContent {
            Page(viewModel = viewModel)
        }
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        CoroutineScope(Dispatchers.Main).launch {
            viewModel.navigationEvents.collect { event ->
                if(event is NavigationEvents.OpenActivity) {

                    if(event.activity == WriteTag::class.java) {
                        if (!viewModel.memory.device.isExist) {
                            this@MainActivity.startActivity(Intent(this@MainActivity, DeviceRegister::class.java))
                        } else {
                            this@MainActivity.startActivity(Intent(this@MainActivity, WriteTag::class.java))
                            this@MainActivity.startService(Intent(this@MainActivity, IotHub::class.java))
                        }
                    } else {
                        Intent(this@MainActivity, event.activity).apply {
                            if (!event.data.isNullOrEmpty()) {
                                putExtra("data", event.data)
                            }
                            this@MainActivity.startActivity(this)
                        }
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            viewModel.navigationEvents.collectLatest { event ->
                if(event is NavigationEvents.OpenService) {
                    this@MainActivity.startService(Intent(this@MainActivity, event.service))
                }
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
}

@Composable
private fun Page(viewModel: MainViewModel) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    if (viewModel.loginMode) LoginAppBar() else AppBarWithNavigationButton(
                        imageVector = Icons.Filled.Person,
                        title = mainTitle,
                        onNavigationButtonPressed = { viewModel.openAccountDialog = true }
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        if (viewModel.loginMode)
                            LoginContent(viewModel = viewModel)
                        else {
                            MainContent(viewModel = viewModel)
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = { if (!viewModel.loginMode) MainBottomBar(viewModel = viewModel) }
            )
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {

    Column {
        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else
            LazyColumn(
                modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    if (viewModel.openAccountDialog) {
                        AlertDialogWith2ButtonAndAppVersion(
                            title = viewModel.userFullName,
                            appVersion = viewModel.appVersion,
                            btnConfirm = "خروج از حساب",
                            btnNotConfirm = "ارسال و دریافت",
                            btnNotConfirmOnClick = {
                                viewModel.openAccountDialog = false
                                viewModel.onSyncButtonClick()
                            },
                            btnConfirmOnClick = {
                                viewModel.openAccountDialog = false
                                viewModel.onLogOutButtonClick()
                            },
                            onDismiss = { viewModel.openAccountDialog = false })
                    }

                    if (viewModel.openSendAndReceiveDialog) {

                        AlertDialogWithHeadlineMediumButton(
                            title = "ارسال و دریافت با موفقیت انجام شد، همگام سازی اطلاعات چند دقیقه زمان می\u200Cبرد.",
                            btnTxt = "متوجه شدم",
                            btnOnClick = { viewModel.openSendAndReceiveDialog = false },
                            onDismiss = { viewModel.openSendAndReceiveDialog = false }
                        )
                    }

                    if (viewModel.isStoreMode) {

                        val numberOfRowsBeforeLastRow = (viewModel.featuresList.size / 4)
                        val numberOfFeaturesInLastRow = (viewModel.featuresList.size % 4)

                        for (rowIndex in 0 until numberOfRowsBeforeLastRow) {

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {

                                for (i in 0..3) {
                                    val it = viewModel.featuresList[rowIndex * 4 + i]
                                    OpenActivityButton(
                                        it.featureTitleResourceAddress,
                                        it.featureIconResourceAddress
                                    ) {
                                        viewModel.onFeatureButtonClick(it.featureClass, null)
                                    }
                                }
                            }
                        }

                        if (numberOfFeaturesInLastRow != 0) {

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {

                                for (i in 0 until numberOfFeaturesInLastRow) {
                                    val it =
                                        viewModel.featuresList[numberOfRowsBeforeLastRow * 4 + i]
                                    OpenActivityButton(
                                        it.featureTitleResourceAddress,
                                        it.featureIconResourceAddress
                                    ) {
                                        viewModel.onFeatureButtonClick(it.featureClass, null)
                                    }
                                }

                                for (i in 0 until (4 - numberOfFeaturesInLastRow)) {
                                    Box(modifier = Modifier.size(80.dp))
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .height(128.dp)
                                .fillMaxWidth()
                        )
                    } else {
                        ExpandableCard("cartonsFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("stockDraftsFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("shelfFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("driverFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("transferFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("mojoodiReviewFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("requestFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("banimodeFeatures", viewModel.featuresList, viewModel)
                        Spacer(
                            modifier = Modifier
                                .height(128.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
    }
}

@Composable
fun MainBottomBar(viewModel: MainViewModel) {

    Box(
        modifier = Modifier
            //.shadow(6.dp, RoundedCornerShape(0.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .height(72.dp)
        //.align(Alignment.BottomCenter),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            FilterDropDownListWithSearch(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_location_city_24),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 4.dp)
                    )
                },
                text = {
                    Text(
                        text = (viewModel.userWarehouse),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 16.dp)
                    )
                },
                onClick = { warehouseTitle ->
                    viewModel.userWarehousesListsOnClick(warehouseTitle)
                },
                values = viewModel.userWarehousesList
            )
        }
    }
}

@Composable
fun LoginContent(viewModel: MainViewModel) {

    Column {

        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else {

            SimpleTextField(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp)
                    .fillMaxWidth(),
                hint = "نام کاربری خود را وارد کنید",
                onValueChange = { viewModel.username = it },
                value = viewModel.username,
            )

            SimpleTextField(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                hint = "رمز عبور خود را وارد کنید",
                onValueChange = { viewModel.password = it },
                value = viewModel.password,
                visualTransformation = PasswordVisualTransformation()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
            ) {

                Text(
                    text = "ورود بدون اینترنت",
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .layoutId(Alignment.Start)
                        .padding(start = 24.dp, end = 24.dp)
                )
                Switch(
                    checked = viewModel.isOfflineMode,
                    onCheckedChange = { viewModel.isOfflineMode = it },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .alignByBaseline()
                )
                if (viewModel.isOfflineMode) {
                    SimpleTextField(
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                        hint = "کد فروشگاه",
                        onValueChange = {
                            viewModel.locationCode = it
                        },
                        value = viewModel.locationCode,
                        keyboardType = KeyboardType.Number,
                    )
                }
            }

            BigButton(text = "ورود") {
                viewModel.login()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginAppBar() {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ورود به حساب کاربری", textAlign = TextAlign.Center,
                )
            }
        },
    )
}

@Preview
@Composable
fun Preview() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(60.dp)
    ) {
        Switch(
            checked = true,
            onCheckedChange = { },
        )
    }

}