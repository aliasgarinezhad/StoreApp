package io.domil.store

import androidx.compose.runtime.Composable
import io.domil.store.view.MainPage
import io.domil.store.viewModel.MainViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val viewModel = MainViewModel()
    ComposableHost(viewModel)
}

@Composable
fun ComposableHost(viewModel: MainViewModel) {

    MainPage(
        state = viewModel.state,
        sizeFilterValue = viewModel.sizeFilterValue,
        sizeFilterValues = viewModel.sizeFilterValues,
        colorFilterValue = viewModel.colorFilterValue,
        colorFilterValues = viewModel.colorFilterValues,
        onImeAction = { viewModel.onImeAction() },
        onScanButtonClick = { viewModel.openCamera() },
        onTextValueChange = { viewModel.onTextValueChange(it) },
        onSizeFilterValueChange = { viewModel.onSizeFilterValueChange(it) },
        onColorFilterValueChange = { viewModel.onColorFilterValueChange(it) },
        onBottomBarButtonClick = { viewModel.openCamera() },
        loading = viewModel.loading,
        isCameraOn = viewModel.isCameraOn,
        textFieldValue = viewModel.productCode,
        uiList = viewModel.filteredUiList
    )
}
