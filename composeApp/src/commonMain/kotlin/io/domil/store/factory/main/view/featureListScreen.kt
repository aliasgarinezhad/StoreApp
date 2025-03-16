package io.domil.store.factory.main.view

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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.domil.store.factory.addTaskFeature.data.FactoryUser
import io.domil.store.factory.main.model.Feature
import io.domil.store.theme.MyApplicationTheme
import io.domil.store.view.ErrorSnackBar
import io.domil.store.view.LoadingIndicator
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource

@Serializable
object FeatureListScreen

@Composable
fun FeatureListScreen(
    state: SnackbarHostState,
    loading: Boolean,
    featuresList: List<Feature>,
    factoryUser: FactoryUser,
    onFeatureIconClick: (screen: Any) -> Unit
) {

    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content = {
                    MainContent(
                        loading = loading,
                        featuresList = featuresList,
                        onFeatureIconClick = onFeatureIconClick,
                        factoryUser = factoryUser
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
    onFeatureIconClick: (screen: Any) -> Unit
) {

    Column {
        if (loading) {
            LoadingIndicator()
        } else

            Text(factoryUser.fullName, modifier = Modifier.padding(top = 16.dp, start = 16.dp))

        LazyColumn(
            modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp),
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