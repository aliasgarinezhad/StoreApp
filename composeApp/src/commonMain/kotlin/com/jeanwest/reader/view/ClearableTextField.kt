package com.jeanwest.reader.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.contracts.SimpleEffect

@Composable
fun ClearAbleTextField(
    modifier: Modifier,
    hint: String,
    onValueChange: (it: String) -> Unit,
    value: String,
    isError: Boolean = false,
    onDone: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextFieldFocused: () -> Unit
) {

    var isFocused by remember { mutableStateOf(false) }
    SimpleTextField(
        modifier = modifier.onFocusChanged {
            isFocused = it.isFocused
            if (isFocused) {
                onTextFieldFocused()
            }
        },
        value = value,
        onValueChange = onValueChange,
        hint = hint,
        isError = isError,
        onDone = onDone,
        keyboardType = keyboardType,
        visualTransformation = visualTransformation,

    )
}


@Composable
fun SimpleTextField(
    modifier: Modifier,
    hint: String,
    onValueChange: (it: String) -> Unit,
    value: String,
    isError: Boolean = false,
    onDone: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        //textStyle = MaterialTheme.typography.bodyMedium,
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        modifier = modifier
            .testTag("TextField")
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            onDone()
        }),
        label = { Text(text = hint) },
        isError = isError,
        visualTransformation = visualTransformation,
        singleLine = true,

        )
}
