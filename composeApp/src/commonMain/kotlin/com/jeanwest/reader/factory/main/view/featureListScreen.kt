package com.jeanwest.reader.factory.main.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.factory.addTaskFeature.data.FactoryUser
import com.jeanwest.reader.factory.main.model.Feature
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingIndicator
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource

@Serializable
object FeatureListScreen

/**
 * Composable function that displays the list of features available to the user.
 *
 * @param state The state of the Snackbar host, used to display error messages.
 * @param loading A boolean indicating whether the feature list is currently loading.
 * @param featuresList The list of [Feature] objects to display.
 * @param factoryUser The [FactoryUser] object associated with the current user.
 * @param textFieldValue The current value of the search text field.
 * @param onTextFieldChanged Callback function invoked when the search text field value changes.  Takes the new value as a parameter.
 * @param onFeatureIconClick Callback function invoked when a feature icon is clicked. Takes the screen associated with the feature as a parameter (type `Any`).
 */
@Composable
fun FeatureListScreen(
    state: SnackbarHostState,
    loading: Boolean,
    featuresList: List<Feature>,
    factoryUser: FactoryUser,
    textFieldValue: String,
    onTextFieldChanged: (value: String) -> Unit,
    onFeatureIconClick: (screen: Any) -> Unit,
    onTextFieldFocused: () -> Unit,
    ) {

    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content = {
                    MainContent(
                        loading = loading,
                        featuresList = featuresList,
                        onFeatureIconClick = onFeatureIconClick,
                        factoryUser = factoryUser,
                        textFieldValue = textFieldValue,
                        onTextFieldChanged = onTextFieldChanged,
                        onTextFieldFocused = onTextFieldFocused,
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
fun MainContent(
    loading: Boolean,
    featuresList: List<Feature>,
    factoryUser: FactoryUser,
    textFieldValue: String,
    onTextFieldChanged: (value: String) -> Unit,
    onFeatureIconClick: (screen: Any) -> Unit,
    onTextFieldFocused: () -> Unit,
) {

    Column {
        if (loading) {
            LoadingIndicator()
        } else {

            Row {
                Text(
                    factoryUser.fullName,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp)
                        .align(Alignment.CenterVertically)
                )

                var isFocused by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        // Allow only digits
                        onTextFieldChanged(newValue)
                    },
                    label = { Text("شماره چرخ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth()
                        .onFocusChanged {
                            isFocused = it.isFocused
                            if (isFocused) {
                                onTextFieldFocused()
                            }
                        },
                )
            }

            LazyColumn(
                modifier = Modifier.padding(top = 32.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.Top
            ) {
                item {

                    val numberOfRowsBeforeLastRow = (featuresList.size / 3)
                    val numberOfFeaturesInLastRow = (featuresList.size % 3)

                    for (rowIndex in 0 until numberOfRowsBeforeLastRow) {

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {

                            for (i in 0..2) {
                                val it = featuresList[rowIndex * 3 + i]
                                OpenActivityButton(
                                    title = it.title,
                                    icon = painterResource(it.iconRes),
                                ) {
                                    onFeatureIconClick(it.routeScreen)
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
                                    featuresList[numberOfRowsBeforeLastRow * 3 + i]
                                OpenActivityButton(
                                    title = it.title,
                                    icon = painterResource(it.iconRes),
                                ) {
                                    onFeatureIconClick(it.routeScreen)
                                }
                            }

                            for (i in 0 until (3 - numberOfFeaturesInLastRow)) {
                                Box(modifier = Modifier.size(80.dp))
                            }
                        }
                    }
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
fun OpenActivityButton(title: String, icon: Painter, onClick: () -> Unit) {

    val iconSize = 88.dp
    val textSize = 96.dp

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                onClick()
            }
    ) {

        Icon(
            painter = icon, contentDescription = "", modifier = Modifier
                .size(iconSize)
                .align(Alignment.CenterHorizontally)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = MaterialTheme.shapes.large
                )
                .padding(4.dp), tint = MaterialTheme.colors.onPrimary
        )
        Text(
            title,
            modifier = Modifier
                .width(textSize)
                .padding(top = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )
    }
}