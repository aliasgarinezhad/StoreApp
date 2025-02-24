package io.domil.store.factory.addTaskFeature.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.domil.store.factory.addTaskFeature.model.ProductionLine
import io.domil.store.theme.MyApplicationTheme
import io.domil.store.theme.Shapes
import io.domil.store.view.ErrorSnackBar
import kotlinx.serialization.Serializable

@Serializable
object SelectTaskScreen

@Composable
fun SelectTaskScreen(
    productionLine: ProductionLine,
    loading: Boolean,
    state: SnackbarHostState,
    onClick: (task: String) -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content = {
                    Content(
                        loading = loading,
                        productionLine = productionLine,
                        onClick = onClick
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
private fun Content(
    loading: Boolean,
    productionLine: ProductionLine,
    onClick: (task: String) -> Unit,
) {
    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
        item(productionLine.tasks.size) {
            productionLine.tasks.forEach { task ->
                Text(
                    text = task,
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .shadow(4.dp, Shapes.medium)
                        .background(
                            color = MaterialTheme.colors.onPrimary,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(top = 8.dp, bottom = 8.dp, start = 8.dp)
                        .fillMaxWidth()
                )
            }
            Spacer(
                modifier = Modifier
                    .height(128.dp)
                    .fillMaxWidth()
            )
        }
    }
}
