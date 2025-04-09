package io.domil.store.factory.addTaskFeature.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.sp
import io.domil.store.factory.addTaskFeature.model.Product
import io.domil.store.view.MyApplicationTheme
import io.domil.store.view.Shapes
import io.domil.store.view.ErrorSnackBar
import kotlinx.serialization.Serializable

@Serializable
object SelectTaskScreen

/**
 * Composable function that displays a screen for selecting a task related to a product.
 *
 * @param product The [Product] for which tasks are being selected.
 * @param loading A boolean flag indicating whether data is currently being loaded. If true, a loading indicator is displayed.
 * @param state The [SnackbarHostState] used to manage and display Snackbars for error messages or other feedback.
 * @param onClick A lambda function that is called when a task is selected.  It receives the selected task string as a parameter.
 * @param pageTitle The title to be displayed in the app bar.
 * @param onBack A lambda function that is called when the back button in the app bar is pressed.
 */
@Composable
fun SelectTaskScreen(
    product: Product,
    loading: Boolean,
    state: SnackbarHostState,
    onClick: (task: String) -> Unit,
    pageTitle: String,
    onBack: () -> Unit
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
                        product = product,
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
    product: Product,
    onClick: (task: String) -> Unit,
) {

    Column {

        Row(modifier = Modifier.padding(top = 16.dp, start = 16.dp)) {

            Text(
                text = "استایل: ",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Right,
            )

            Text(
                text = product.style.substring(product.style.length - 3, product.style.length) + "-",
                style = MaterialTheme.typography.h1,
                textAlign = TextAlign.Right,
                fontSize = 14.sp,
            )
            Text(
                text = product.style.substring(0 , product.style.length - 3),
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Right,
            )

            Text(
                text = "رنگ: " + product.color,
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Right,
            )
        }

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            item(product.tasks.size) {
                product.tasks.forEach { task ->
                    Text(

                        text = task.key,
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .shadow(4.dp, Shapes.medium)
                            .background(
                                color = MaterialTheme.colors.onPrimary,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { onClick(task.key) }
                            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp)
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
}
