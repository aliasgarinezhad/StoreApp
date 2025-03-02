package io.domil.store.factory.addTaskFeature.view

import Picker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.domil.store.factory.addTaskFeature.model.ProductionLine
import kotlinx.serialization.Serializable
import rememberPickerState

@Serializable
object EnterDateAndNumberScreen

@Composable
fun EnterDateAndNumberScreen(
    loading: Boolean,
    productionLine: ProductionLine,
    state: SnackbarHostState
) {
    // Define hour and minute values for the pickers
    val hourValues = (0..23).map { it.toString() }
    val minuteValues = (0..59).map { it.toString() }

    // States for start time
    val startHourPickerState = rememberPickerState()
    val startMinutePickerState = rememberPickerState()

    // States for end time
    val endHourPickerState = rememberPickerState()
    val endMinutePickerState = rememberPickerState()

    // State for dropdown selection
    var expanded by remember { mutableStateOf(false) }
    val sizeOptions = listOf("Small", "Medium", "Large")
    var selectedSize by remember { mutableStateOf(sizeOptions.first()) }

    // State for integer input text
    var inputValue by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        color = Color.Transparent
    ) {
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
                // Dropdown for size selection
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "انتخاب سایز",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedSize,
                            onValueChange = { /* Read-only */ },
                            label = { Text("انتخاب سایز") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            sizeOptions.forEach { size ->
                                DropdownMenuItem(onClick = {
                                    selectedSize = size
                                    expanded = false
                                }) {
                                    Text(text = size)
                                }
                            }
                        }
                    }
                }
                // Input field for quantity
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تعداد",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { newValue ->
                            // Allow only digits
                            inputValue = newValue.filter { it.isDigit() }
                        },
                        label = { Text("تعداد") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}