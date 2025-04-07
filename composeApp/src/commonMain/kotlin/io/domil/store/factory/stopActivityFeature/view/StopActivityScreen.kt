package io.domil.store.factory.stopActivityFeature.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
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
import io.domil.store.factory.addTaskFeature.view.AppBarWithBack
import io.domil.store.factory.stopActivityFeature.model.Reason
import io.domil.store.theme.MyApplicationTheme
import io.domil.store.view.ErrorSnackBar
import io.domil.store.view.LoadingIndicator
import kotlinx.serialization.Serializable

@Serializable
object StopActivityScreen

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
                            selectedColor = MaterialTheme.colors.primary
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
