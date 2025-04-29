package com.jeanwest.reader.login.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.view.BigButton
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.MyApplicationTheme
import kotlinx.serialization.Serializable


@Serializable
object LoginScreen

/**
 * Composable function for the login page.
 *
 * @param username The current value of the username input field.
 * @param password The current value of the password input field.
 * @param onSignInButtonClick Callback function to be executed when the sign-in button is clicked.
 * @param onUsernameValueChanged Callback function to be executed when the username input value changes.  It receives the new username value as a parameter.
 * @param onPasswordValueChanged Callback function to be executed when the password input value changes. It receives the new password value as a parameter.
 * @param state The [SnackbarHostState] to control the display of snackbars for error messages.
 * @param loading A boolean indicating whether a loading indicator should be displayed.  True displays the indicator, false hides it.
 */
@Composable
fun LoginPage(
    username: String,
    password: String,
    onSignInButtonClick: () -> Unit,
    onUsernameValueChanged: (value: String) -> Unit,
    onPasswordValueChanged: (value: String) -> Unit,
    state: SnackbarHostState,
    loading: Boolean,
    isFactoryModeRequested: Boolean,
    onIsFactoryModeChanged: (value: Boolean) -> Unit,
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
                        loading = loading,
                        isFactoryModeRequested = isFactoryModeRequested,
                        onIsFactoryModeChanged = onIsFactoryModeChanged
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
    loading: Boolean,
    isFactoryModeRequested: Boolean,
    onIsFactoryModeChanged: (value: Boolean) -> Unit,
) {

    val focus = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            Row(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
            ) {

                Text(
                    text = "کاربر تولیدی",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 24.dp, end = 24.dp)
                )
                Switch(
                    checked = isFactoryModeRequested,
                    onCheckedChange = onIsFactoryModeChanged,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .alignByBaseline()
                )
            }
            BigButton(
                text = "ورود به حساب کاربری",
                onClick = {
                    focus.clearFocus()
                    onSignInButtonClick()
                },
            )
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
            .padding(top = 32.dp, start = 16.dp, end = 16.dp)
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
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            .fillMaxWidth(),
        label = { Text(text = "رمز عبور") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.NumberPassword
        ),
        singleLine = true
    )
}