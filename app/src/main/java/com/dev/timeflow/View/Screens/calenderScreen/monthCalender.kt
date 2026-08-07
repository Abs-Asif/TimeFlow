package com.dev.timeflow.View.Screens.calenderScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.dev.timeflow.Data.Model.Events
import java.time.LocalDate

@Composable
fun MonthCalender(
    day: CalendarDay,
    modifier: Modifier = Modifier,
    hapticFeedback: HapticFeedback,
    selectedDate : LocalDate,
    activeEvents : List<Events> = emptyList(),
    onClick : (LocalDate) -> Unit
) {
    val date = LocalDate.now()
    val dayPosition = day.position == DayPosition.MonthDate
    val isSelected = selectedDate == day.date
    val isToday = date == day.date
    val isFriday = day.date.dayOfWeek == java.time.DayOfWeek.FRIDAY

    val boxSelectedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "BoxSelectedColor"
    )

    val boxTextColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isToday -> MaterialTheme.colorScheme.primary
            isFriday -> Color.Red
            dayPosition -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.2f
            )
        },
        label = "BoxTextColor"
    )

    // Using circular-square (more square, rounded corner) Shape: RoundedCornerShape(10.dp)
    val dayCellShape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(4.dp)
                .clip(dayCellShape)
                .border(
                    width = if (isToday) 1.5.dp else 0.dp,
                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = dayCellShape
                )
                .background(
                    boxSelectedColor
                )
                .clickable(
                    onClick = {
                        onClick(
                            day.date
                        )
                        hapticFeedback.performHapticFeedback(
                            hapticFeedbackType = HapticFeedbackType.Confirm
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(0.dp),
                    text = day.date.dayOfMonth.toString(),
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    color = boxTextColor
                )
            }
        }

        if (activeEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                activeEvents.forEach { event ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(event.colorHex)),
                                shape = RoundedCornerShape(1.5.dp)
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun MonthHeader(
    modifier: Modifier = Modifier,
    weekName : List<String>
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    bottom = 8.dp
                )
        ) {
            weekName.forEach {
                val isFri = it == "Fri"
                Text(
                    modifier = modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 15.sp,
                    text = it,
                    color = if (isFri) Color.Red else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isFri) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
