package io.domil.store.factory.addTaskFeature.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Displays an AppBar with a back arrow icon for navigation.
 *
 * This composable provides a convenient way to create a standard app bar with a back button.
 * It uses the `AppBarWithNavigationButton` composable internally and pre-configures it
 * with a back arrow icon and the provided title and navigation action.
 *
 * @param onBackPressed Callback to be executed when the back button is pressed.  Defaults to an empty lambda.
 * @param title The title to display in the app bar.
 */
@Composable
fun AppBarWithBack(
    onBackPressed: () -> Unit = {},
    title: String,
) {
    AppBarWithNavigationButton(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        title = title,
        onNavigationButtonPressed = { onBackPressed() },
        testTag = "back"
    )
}

@Composable
fun AppBarWithNavigationButton(
    imageVector: ImageVector,
    title: String,
    onNavigationButtonPressed: () -> Unit = {},
    testTag: String = "",
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                modifier = Modifier.testTag("navigation"),
                onClick = { onNavigationButtonPressed() }) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = "",
                    Modifier.testTag(
                        if (testTag != "") testTag else ""
                    )
                )
            }
        },

        title = {
            Text(
                text = title,
                modifier = Modifier
                    .padding(end = 50.dp)
                    .fillMaxSize()
                    .wrapContentSize(),
                textAlign = TextAlign.Right,
            )
        }
    )
}