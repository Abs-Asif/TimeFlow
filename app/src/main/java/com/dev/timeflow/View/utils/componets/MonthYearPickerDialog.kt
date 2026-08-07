package com.dev.timeflow.View.utils.componets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthYearPickerDialog(
    initialMonth: Month,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Month, Int) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedYear by remember { mutableStateOf(initialYear) }

    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    val months = Month.values()
    val years = (2020..2040).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Month & Year",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month Selector
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { monthMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = selectedMonth.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            maxLines = 1
                        )
                    }
                    DropdownMenu(
                        expanded = monthMenuExpanded,
                        onDismissRequest = { monthMenuExpanded = false },
                        modifier = Modifier.heightIn(max = 250.dp)
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = month.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                                },
                                onClick = {
                                    selectedMonth = month
                                    monthMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Year Selector
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { yearMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = selectedYear.toString())
                    }
                    DropdownMenu(
                        expanded = yearMenuExpanded,
                        onDismissRequest = { yearMenuExpanded = false },
                        modifier = Modifier.heightIn(max = 250.dp)
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(text = year.toString()) },
                                onClick = {
                                    selectedYear = year
                                    yearMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMonth, selectedYear) }
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
