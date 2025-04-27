package com.jeanwest.reader.factory.stopActivityFeature.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.factory.addTaskFeature.view.AppBarWithBack
import com.jeanwest.reader.factory.stopActivityFeature.model.Reason
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingIndicator
import kotlinx.serialization.Serializable

@Serializable
object StopActivityScreen

/**
 * Composable function for the "Stop Activity" screen.  Allows the user to select a reason
 * for stopping an activity and submit it. Displays a loading indicator during submission
 * and handles errors with a Snackbar.
 *
 * @param loading Boolean flag indicating whether a submission is in progress.  Disables
 *                interaction with the content while true and shows a loading indicator.
 * @param state SnackbarHostState for displaying error messages via a Snackbar.
 * @param pageTitle String to display as the title in the top app bar.
 * @param onBack Lambda function to be executed when the back button in the app bar is pressed.  Usually
 *               used for navigating back to the previous screen.
 * @param reasons List of Reason objects representing the possible reasons for stopping the activity.
 * @param onConfirm Lambda function to be executed when the user confirms their selection.  It takes the
 *                  ID of the selected reason as an integer parameter.
 */
@Composable
fun StopActivityScreen(
    loading: Boolean,
    state: SnackbarHostState,
    pageTitle: String,
    onBack: () -> Unit,
    reasons: List<Reason>,
    onConfirm: (selectedReasonId: Int) -> Unit
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(title = pageTitle, onBackPressed = onBack)
                },
                content = {
                    Content(
                        loading = loading,
                        reasons = reasons,
                        onConfirm = onConfirm
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
fun Content(
    loading: Boolean,
    reasons: List<Reason>,
    onConfirm: (selectedReasonId: Int) -> Unit
) {
    if (loading) {
        LoadingIndicator()
    } else {
        // Hold the id of the selected reason
        var selectedId by remember { mutableStateOf<Int?>(null) }

        // In RTL, Alignment.Start aligns the content to the right
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            reasons.forEach { reason ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (reason.id == selectedId),
                        onClick = { selectedId = reason.id },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = reason.text,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { selectedId?.let { onConfirm(it) } },
                enabled = selectedId != null
            ) {
                Text(text = "تأیید")
            }
        }
    }
}
