package io.domil.store

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.domil.store.theme.ErrorSnackBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class UserLoginActivity : ComponentActivity() {

    private var username by mutableStateOf("")
    private var password by mutableStateOf("")
    private var state = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContent { Page() }
    }
    /*
            editor.putInt("userLocationCode", response.getInt("locationCode"))
            editor.putInt(
                "userWarehouseCode",
                response.getJSONObject("location").getInt("warehouseCode")
            )

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("username", username)
                body.put("password", password)
                return body.toString().toByteArray()
            }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun Page() {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = { AppBar() },
                    content = { Content() },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @Composable
    fun Content() {

        val focus = LocalFocusManager.current

        Column(modifier = Modifier.fillMaxSize()) {
            UsernameTextField()
            PasswordTextField()
            Button(
                onClick = {
                    focus.clearFocus()
                    signIn()
                },
                modifier = Modifier
                    .padding(top = 20.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "ورود")
            }
        }
    }

    @Composable
    fun AppBar() {

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

    @Composable
    fun UsernameTextField() {

        OutlinedTextField(
            value = username, onValueChange = {
                username = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "نام کاربری") }
        )
    }

    @Composable
    fun PasswordTextField() {

        OutlinedTextField(
            value = password, onValueChange = {
                password = it
            },
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            label = { Text(text = "رمز عبور") }
        )
    }

 */

}