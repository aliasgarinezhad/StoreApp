package io.domil.store.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.domil.store.theme.doneColor
import io.domil.store.theme.errorColor
import io.domil.store.theme.warningColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * A composable function that displays a snackbar for showing errors, success, or warning messages.
 * The snackbar is anchored to the bottom of the screen and provides a dismiss action.
 * The color of the message text is determined by the action label provided via the SnackbarHostState.
 *
 * @param state The [SnackbarHostState] that manages the snackbar's visibility and data.  The state's
 * `currentSnackbarData` property provides access to the message and action label.
 *
 *  The `actionLabel` is used to determine the message color:
 *    - "ERROR" (from `SnackBarActions.ERROR.toString()`):  Displays the message in `errorColor`.
 *    - "SUCCESS" (from `SnackBarActions.SUCCESS.toString()`): Displays the message in `doneColor`.
 *    - "WARNING" (from `SnackBarActions.WARNING.toString()`): Displays the message in `warningColor`.
 *    - Any other value or null: Displays the message in `errorColor` (default).
 *
 * The dismiss action is labeled "متوجه شدم" (Understood) and closes the snackbar when clicked.
 */
@Composable
fun ErrorSnackBar(state: SnackbarHostState) {

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {

        SnackbarHost(hostState = state, snackbar = {
            Snackbar(
                shape = MaterialTheme.shapes.large,
                action = {
                    Text(
                        text = "متوجه شدم",
                        color = MaterialTheme.colors.secondary,
                        style = MaterialTheme.typography.h2,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable {
                                state.currentSnackbarData?.dismiss()
                            }
                    )
                }
            ) {
                Text(
                    text = state.currentSnackbarData?.message ?: "",
                    color = when (state.currentSnackbarData?.actionLabel) {
                        SnackBarActions.ERROR.toString() -> {
                            errorColor
                        }

                        SnackBarActions.SUCCESS.toString() -> {
                            doneColor
                        }

                        SnackBarActions.WARNING.toString() -> {
                            warningColor
                        }

                        else -> {
                            errorColor
                        }
                    },
                    style = MaterialTheme.typography.h2,
                )
            }
        })
    }
}

enum class SnackBarActions {
    ERROR,
    WARNING,
    SUCCESS
}

fun showLog(
    data: String,
    state: SnackbarHostState,
    action: SnackBarActions = SnackBarActions.ERROR,
) {
    CoroutineScope(Dispatchers.Main).launch {
        state.currentSnackbarData?.dismiss()
        state.showSnackbar(
            data,
            action.toString(),
            if (action == SnackBarActions.WARNING) SnackbarDuration.Indefinite else SnackbarDuration.Short
        )
    }
}

