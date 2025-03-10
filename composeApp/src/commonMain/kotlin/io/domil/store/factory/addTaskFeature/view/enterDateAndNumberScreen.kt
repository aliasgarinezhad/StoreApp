package io.domil.store.factory.addTaskFeature.view

import Picker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.domil.store.factory.addTaskFeature.model.Product
import io.domil.store.factory.addTaskFeature.model.UserTask
import io.domil.store.theme.BottomBar
import io.domil.store.theme.Jeanswest
import io.domil.store.theme.MyApplicationTheme
import io.domil.store.theme.Shapes
import io.domil.store.theme.borderColor
import io.domil.store.theme.iconColor
import io.domil.store.view.ErrorSnackBar
import kotlinx.serialization.Serializable
import rememberPickerState

@Serializable
object EnterDateAndNumberScreen

@Composable
fun EnterDateAndNumberScreen(
    loading: Boolean,
    product: Product,
    state: SnackbarHostState,
    onClick: () -> Unit,
    onSizeSelected: (size: String) -> Unit,
    onTextFieldChanged: (value: String) -> Unit,
    onStartHourChanged: (startHour: String) -> Unit,
    onStartMinuteChanged: (startHour: String) -> Unit,
    onEndHourChanged: (endHour: String) -> Unit,
    onEndMinuteChanged: (endMinute: String) -> Unit,
    userTask: UserTask,
) {
    // Define hour and minute values for the pickers
    val hourValues = (7..18).map { it.toString() }
    val minuteValues = listOf(0, 15, 30, 45).map { it.toString() }

    // States for start time
    val startHourPickerState = rememberPickerState()
    val startMinutePickerState = rememberPickerState()

    // States for end time
    val endHourPickerState = rememberPickerState()
    val endMinutePickerState = rememberPickerState()

    // State for dropdown selection
    var expanded by remember { mutableStateOf(false) }

    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content =  {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Start Time Section
                        Text(
                            text = "ساعت شروع",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Column for Hour Picker with caption
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ساعت",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold
                                )
                                Picker(
                                    items = hourValues,
                                    state = startHourPickerState,
                                    visibleItemsCount = 5,
                                    modifier = Modifier.fillMaxWidth(),
                                    textModifier = Modifier.padding(8.dp),
                                    textStyle = TextStyle(fontSize = 24.sp)
                                )
                            }
                            // Column for Minute Picker with caption
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "دقیقه",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold
                                )
                                Picker(
                                    items = minuteValues,
                                    state = startMinutePickerState,
                                    visibleItemsCount = 5,
                                    modifier = Modifier.fillMaxWidth(),
                                    textModifier = Modifier.padding(8.dp),
                                    textStyle = TextStyle(fontSize = 24.sp)
                                )
                            }
                        }

                        // End Time Section
                        Text(
                            text = "ساعت پایان",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Column for Hour Picker with caption
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ساعت",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold
                                )
                                Picker(
                                    items = hourValues,
                                    state = endHourPickerState,
                                    visibleItemsCount = 5,
                                    modifier = Modifier.fillMaxWidth(),
                                    textModifier = Modifier.padding(8.dp),
                                    textStyle = TextStyle(fontSize = 24.sp)
                                )
                            }
                            // Column for Minute Picker with caption
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "دقیقه",
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold
                                )
                                Picker(
                                    items = minuteValues,
                                    state = endMinutePickerState,
                                    visibleItemsCount = 5,
                                    modifier = Modifier.fillMaxWidth(),
                                    textModifier = Modifier.padding(8.dp),
                                    textStyle = TextStyle(fontSize = 24.sp)
                                )
                            }
                        }

                        // Size and Quantity Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FilterDropDownList(
                                modifier = Modifier
                                    .padding(start = 16.dp, bottom = 16.dp),
                                icon = {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "",
                                        tint = iconColor,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .align(Alignment.CenterVertically)
                                            .padding(start = 6.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = userTask.size,
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(start = 6.dp)
                                    )
                                },
                                onClick = {
                                    onSizeSelected(it)
                                },
                                values = userTask.product.tasks
                            )
                            // Input field for quantity
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تعداد",
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                                OutlinedTextField(
                                    value = userTask.number.toString(),
                                    onValueChange = { newValue ->
                                        // Allow only digits
                                        onTextFieldChanged(newValue.filter { it.isDigit() })
                                    },
                                    label = { Text("تعداد") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}


@Composable
fun FilterDropDownList(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    text: @Composable () -> Unit,
    values: List<String>,
    onClick: (item: String) -> Unit,
) {

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colors.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .border(
                BorderStroke(1.dp, if (expanded) Jeanswest else borderColor),
                shape = MaterialTheme.shapes.small
            )
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .testTag("FilterDropDownList")
                .fillMaxHeight(),
        ) {

            icon()
            text()
            Icon(

                if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    }
                ,
                "",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 0.dp, end = 4.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .background(color = BottomBar, shape = Shapes.small)
                .align(Alignment.Center)
        ) {
            values.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onClick(it)
                }) {
                    Text(text = it)
                }
            }
        }
    }
}