package com.jeanwest.reader.features.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Composable function that displays an error snackbar at the bottom of the screen.
 *
 * The snackbar's appearance (text color) is determined by the action label of the current
 * snackbar data in the provided [SnackbarHostState].
 *
 * @param state The [SnackbarHostState] that manages the display of the snackbar.  The message
 *              displayed in the snackbar will be taken from `state.currentSnackbarData?.visuals?.message`,
 *              and the color will be determined based on `state.currentSnackbarData?.visuals?.actionLabel`.
 *              If the action label is "ERROR", the color will be [errorLight].  If it is "SUCCESS",
 *              the color will be [doneColor].  If it is "WARNING", the color will be [warningColor].
 *              Otherwise, the color defaults to [errorLight]. The snackbar will also include a
 *              dismiss button labeled "متوجه شدم" (Understood).
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
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable {
                                state.currentSnackbarData?.dismiss()
                            }
                    )
                }
            ) {
                Text(
                    text = state.currentSnackbarData?.visuals?.message ?: "",
                    color = when (state.currentSnackbarData?.visuals?.actionLabel) {
                        SnackBarActions.ERROR.toString() -> {
                            errorLight
                        }

                        SnackBarActions.SUCCESS.toString() -> {
                            doneColor
                        }

                        SnackBarActions.WARNING.toString() -> {
                            warningColor
                        }

                        else -> {
                            errorLight
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
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
            duration = if (action == SnackBarActions.WARNING) SnackbarDuration.Indefinite else SnackbarDuration.Long
        )
    }
}

