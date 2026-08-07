package com.dev.timeflow.View.utils.componets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    val months = Month.values()
    val years = (1900..2200).toList()

    val listState = rememberLazyListState()

    // Scroll to the active year when the picker is shown
    LaunchedEffect(key1 = initialYear) {
        val index = years.indexOf(initialYear)
        if (index >= 0) {
            // Centers the item roughly in a list height of ~180.dp (each item is ~44.dp)
            listState.scrollToItem(maxOf(0, index - 2))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black, // True AMOLED black background to match app theme
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Title
                Text(
                    text = "Jump to Date",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Scrollable list of years
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(years) { year ->
                                val isSelected = year == selectedYear
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedYear = year },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = year.toString(),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = if (isSelected) 18.sp else 16.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: 3x4 grid of months
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (row in 0 until 4) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    for (col in 0 until 3) {
                                        val monthIndex = row * 3 + col
                                        val month = months[monthIndex]
                                        val isSelected = month == selectedMonth
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .border(
                                                    width = if (isSelected) 0.dp else 1.dp,
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedMonth = month },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel and Confirm actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedMonth, selectedYear) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Confirm",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}