package io.domil.store.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.domil.store.theme.ErrorSnackBar
import io.domil.store.theme.MyApplicationTheme
import kotlinx.serialization.Serializable


@Serializable
object LoginScreen

@Composable
fun LoginPage(
    username: String,
    password: String,
    onSignInButtonClick: () -> Unit,
    onUsernameValueChanged: (value: String) -> Unit,
    onPasswordValueChanged: (value: String) -> Unit,
    state: SnackbarHostState,
    loading: Boolean
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content = {
                    Content(
                        username = username,
                        password = password,
                        onSignInButtonClick = onSignInButtonClick,
                        onUsernameValueChanged = onUsernameValueChanged,
                        onPasswordValueChanged = onPasswordValueChanged,
                        loading = loading
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
fun Content(
    username: String,
    password: String,
    onSignInButtonClick: () -> Unit,
    onUsernameValueChanged: (value: String) -> Unit,
    onPasswordValueChanged: (value: String) -> Unit,
    loading: Boolean
) {

    val focus = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            Row(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colors.primary)
                Text(
                    text = "در حال بارگذاری",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        } else {
        UsernameTextField(
            username = username,
            onUsernameValueChanged = onUsernameValueChanged
        )
        PasswordTextField(
            password = password,
            onPasswordValueChanged = onPasswordValueChanged
        )
        Button(
            onClick = {
                focus.clearFocus()
                onSignInButtonClick()
            },
            modifier = Modifier
                .padding(top = 20.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = "ورود به حساب کاربری")
        }
    }
}
}

@Composable
fun UsernameTextField(
    username: String,
    onUsernameValueChanged: (value: String) -> Unit
) {

    OutlinedTextField(
        value = username, onValueChange = { onUsernameValueChanged(it) },
        modifier = Modifier
            .padding(top = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        label = { Text(text = "نام کاربری") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}

@Composable
fun PasswordTextField(
    password: String,
    onPasswordValueChanged: (value: String) -> Unit
) {
    OutlinedTextField(
        value = password, onValueChange = { onPasswordValueChanged(it) },
        modifier = Modifier
            .padding(top = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        label = { Text(text = "رمز عبور") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}