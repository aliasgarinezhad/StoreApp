package com.jeanwest.reader.features.cargo.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.jeanwest.reader.R
import com.jeanwest.reader.features.cargo.viewmodel.CargoRegistrationViewModel
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 *  CargoRegistration Activity.
 *
 *  This activity handles the registration of cargo. It utilizes a ViewModel ([CargoRegistrationViewModel])
 *  to manage the UI logic and data interaction.  It also sets up an exception handler for uncaught
 *  exceptions and manages activity lifecycle events (pausing and resuming).
 */
@AndroidEntryPoint
class CargoRegistration : ComponentActivity() {

    val viewModel: CargoRegistrationViewModel by viewModels()

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
fun Page(viewModel: CargoRegistrationViewModel, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(onBackPressed, stringResource(R.string.CargoRegistration))
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(viewModel)
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = {
                    BottomBarButton(stringResource(R.string.CargoRegistration)) {
                        viewModel.cargoRegistration()
                    }
                }
            )
        }
    }
}

@Composable
fun Content(viewModel: CargoRegistrationViewModel) {
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
                value = viewModel.invoiceNumber,
                hint = "شماره بارنامه",
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 12.dp
                    )
                    .fillMaxWidth(),
                onValueChange = {
                    viewModel.invoiceNumber = it
                },
                onDone = {
                    if (viewModel.invoiceNumber != "") {
                        viewModel.getLocations()
                    }
                })

            FilterDropDownList(
                modifier = Modifier
                    .padding(15.dp)
                    .wrapContentWidth(),
                text = {
                    Text(
                        text = viewModel.styleCode,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                },
                values = viewModel.styleCodes.keys.toList()
            ) {
                viewModel.styleCode = it
            }

            SimpleTextField(
                value = viewModel.qty,
                hint = "تعداد",
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 12.dp
                    )
                    .wrapContentWidth(),
                keyboardType = KeyboardType.Number,
                onValueChange = {
                    if (it.isDigitsOnly()) {
                        viewModel.qty = it
                    }
                },
                onDone = {
                }
            )
        }
    }
}