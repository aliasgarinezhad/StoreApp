package com.jeanwest.reader.features.carton.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavController
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage

//@Serializable
//object ScanCartonScreen

@Composable
fun ScanCartonScreen(
    loading: Boolean,
    value: String,
    onClick: (navControoller: NavController) -> Unit,
    onValueChange: (String) -> Unit,
    state: SnackbarHostState,
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    navController: NavController,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content = {
                    Box(Modifier.padding(it)) {
                        ScanCarton(
                            loading = loading,
                            value = value,
                            textFieldHint = "شماره کارتن",
                            onClick = { onClick(navController) },
                            onValueChange = { onValueChange(it) }
                        )
                    }
                },
                topBar = {
                    AppBarWithBack(title = topBarTitle, onBackPressed = { topBarOnClick() })
                },
                snackbarHost = { ErrorSnackBar(state) }
            )
        }
    }
}

@Composable
fun ScanCarton(
    loading: Boolean,
    value: String,
    textFieldHint: String,
    onClick: () -> Unit,
    onValueChange: (String) -> Unit,
) {
    ScanOrTypeNumberPage(
        loading = loading,
        onClick = onClick,
        value = value,
        onValueChange = { onValueChange(it) },
        item = textFieldHint
    )
}