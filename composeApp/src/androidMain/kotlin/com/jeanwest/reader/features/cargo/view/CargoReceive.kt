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
import com.jeanwest.reader.features.cargo.viewmodel.CargoReceiveViewModel
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.FilterDropDownList
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 *  CargoReceive Activity.
 *
 *  This activity handles the cargo receiving process. It initializes the view model,
 *  sets up the content using Compose, and manages activity lifecycle events.
 *  It also includes an exception handler for unhandled exceptions.
 *
 *  Key features:
 *      - Initializes and observes the [CargoReceiveViewModel].
 *      - Uses Jetpack Compose to build the UI with the [Page] composable.
 *      - Handles Activity lifecycle events ([onPause], [onResume]).
 *      - Implements an exception handler to gracefully handle unhandled exceptions.
 *
 *  Usage:
 *      This activity is typically launched to start the cargo receiving workflow.  The
 *      [Page] composable should define the UI for this workflow, interacting with the
 *      provided [CargoReceiveViewModel].  The `finish()` callback passed to [Page] is
 *      used to close this activity, typically upon successful completion or cancellation
 *      of the workflow.
 */
@AndroidEntryPoint
class CargoReceive : ComponentActivity() {

    val viewModel: CargoReceiveViewModel by viewModels()

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
fun Page(viewModel: CargoReceiveViewModel, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(onBackPressed, stringResource(R.string.CargoReceive))
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(viewModel)
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = {
                    BottomBarButton(stringResource(R.string.CargoReceive)) {
                        viewModel.cargoReceive()
                    }
                }
            )
        }
    }
}

@Composable
fun Content(viewModel: CargoReceiveViewModel) {

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
                    viewModel.getLocations()
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
                viewModel.packingID = viewModel.styleCodes[it] ?: ""
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
                    .fillMaxWidth(),
                keyboardType = KeyboardType.Number,
                onValueChange = {
                    if (it.isDigitsOnly()) {
                        viewModel.qty = it
                    }
                },
                onDone = {
                })
        }
    }
}