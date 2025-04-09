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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
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
import io.domil.store.view.BigButton
import io.domil.store.view.BottomBar
import io.domil.store.view.Jeanswest
import io.domil.store.view.MyApplicationTheme
import io.domil.store.view.Shapes
import io.domil.store.view.borderColor
import io.domil.store.view.iconColor
import io.domil.store.view.ErrorSnackBar
import io.domil.store.view.NotificationPopUp
import io.domil.store.view.NotificationPopupHost
import kotlinx.serialization.Serializable
import rememberPickerState

@Serializable
object EnterDateAndNumberScreen

/**
 * Composable function for the screen where the user enters the date, time and number for a task.
 *
 * This screen allows the user to select the start and end times using pickers, choose a size from a dropdown,
 * and enter a quantity in a text field. It displays product and task information and provides a button to submit the data.
 *
 * @param loading Boolean indicating whether the screen is in a loading state.
 * @param product The [Product] associated with the user task.
 * @param state The [SnackbarHostState] for displaying snack */
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
    pageTitle: String,
    onBack: () -> Unit,
    textFieldValue: String,
    popupHost: NotificationPopupHost,
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
                topBar = {
                    AppBarWithBack(title = pageTitle, onBackPressed = onBack)
                },
                content = {

                    Column {

                        NotificationPopUp(state = popupHost)

                        Row(modifier = Modifier.padding(top = 16.dp, start = 16.dp)) {

                            Text(
                                text = "استایل: ",
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Right,
                            )

                            Text(
                                text = userTask.product.style.substring(product.style.length - 3, product.style.length) + "-",
                                style = MaterialTheme.typography.h1,
                                textAlign = TextAlign.Right,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = userTask.product.style.substring(0 , product.style.length - 3),
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Right,
                            )

                            Text(
                                text = "رنگ: " + userTask.product.color,
                                modifier = Modifier.padding(start = 16.dp),
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Right,
                            )

                            Text(
                                text = "فعالیت: " + userTask.task,
                                modifier = Modifier.padding(start = 16.dp),
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Right,
                            )
                        }
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
                                textAlign = TextAlign.Start
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Column for Minute Picker with caption
                                Column(
                                    modifier = Modifier.width(128.dp).padding(start = 32.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "دقیقه",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Picker(
                                        items = minuteValues,
                                        state = startMinutePickerState,
                                        visibleItemsCount = 3,
                                        textModifier = Modifier.padding(8.dp),
                                        textStyle = TextStyle(fontSize = 18.sp),
                                        onValueChange = { onStartMinuteChanged(it) },
                                        dividerColor = MaterialTheme.colors.primary
                                    )
                                }
                                // Column for Hour Picker with caption
                                Column(
                                    modifier = Modifier.width(128.dp).padding(start = 32.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "ساعت",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Picker(
                                        items = hourValues,
                                        state = startHourPickerState,
                                        visibleItemsCount = 3,
                                        textModifier = Modifier.padding(8.dp),
                                        textStyle = TextStyle(fontSize = 18.sp),
                                        onValueChange = { onStartHourChanged(it) },
                                        dividerColor = MaterialTheme.colors.primary
                                    )
                                }
                            }

                            // End Time Section
                            Text(
                                text = "ساعت پایان",
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Column for Minute Picker with caption
                                Column(
                                    modifier = Modifier.width(128.dp).padding(start = 32.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "دقیقه",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Picker(
                                        items = minuteValues,
                                        state = endMinutePickerState,
                                        visibleItemsCount = 3,
                                        textModifier = Modifier.padding(8.dp),
                                        textStyle = TextStyle(fontSize = 18.sp),
                                        onValueChange = { onEndMinuteChanged(it) },
                                        dividerColor = MaterialTheme.colors.primary
                                    )
                                }

                                // Column for Hour Picker with caption
                                Column(
                                    modifier = Modifier.width(128.dp).padding(start = 32.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "ساعت",
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Picker(
                                        items = hourValues,
                                        state = endHourPickerState,
                                        visibleItemsCount = 3,
                                        textModifier = Modifier.padding(8.dp),
                                        textStyle = TextStyle(fontSize = 18.sp),
                                        onValueChange = { onEndHourChanged(it) },
                                        dividerColor = MaterialTheme.colors.primary
                                    )
                                }
                            }

                            // Size and Quantity Section
                            Row(
                                modifier = Modifier.wrapContentWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .align(Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterDropDownList(
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .height(54.dp)
                                        .padding(top = 6.dp)
                                        .align(Alignment.CenterVertically),
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
                                    onClick = {
                                        onSizeSelected(it)
                                    },
                                    values = userTask.product.sizes.keys.toList(),
                                    currentSelection = userTask.size,
                                    defaultText = "انتخاب سایز",
                                )
                                OutlinedTextField(
                                    value = textFieldValue,
                                    onValueChange = { newValue ->
                                        // Allow only digits
                                        onTextFieldChanged(newValue)
                                    },
                                    label = { Text("تعداد") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(102.dp).height(60.dp)
                                        .padding(start = 20.dp)
                                )
                            }
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(state) },
                bottomBar = {
                    BigButton(
                        text = "ثبت اطلاعات",
                        onClick = {
                            onClick()
                        },
                    )
                }
            )

        }
    }
}


@Composable
fun FilterDropDownList(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    currentSelection: String, // current selected value (empty if nothing selected)
    defaultText: String = "Select your size",
    values: List<String>,
    onClick: (item: String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    // If nothing is selected, show default text
    val displayText = if (currentSelection.isEmpty()) defaultText else currentSelection

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(
                text = displayText,
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 6.dp)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = "",
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
            // Only show the selectable options (the default text is not listed)
            values.forEach { value ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onClick(value)
                }) {
                    Text(text = value)
                }
            }
        }
    }
}