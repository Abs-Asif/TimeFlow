package com.dev.timeflow.View.Screens.calenderScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.timeflow.Data.Model.Events
import com.dev.timeflow.View.utils.toLocalDate
import com.kizitonwose.calendar.core.WeekDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekCalender(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onClick: (LocalDate) -> Unit,
    weekDate : WeekDay,
    activeEvents : List<Events> = emptyList(),
    eventLanes : Map<Long, Int> = emptyMap()
) {
    val date = LocalDate.now()
    val isSelected = selectedDate == weekDate.date
    val isToday = date == weekDate.date
    val isFriday = weekDate.date.dayOfWeek == java.time.DayOfWeek.FRIDAY

    val boxSelectedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "BoxSelectedColor"
    )

    val boxTextColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isToday -> MaterialTheme.colorScheme.primary
            isFriday -> Color.Red
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "BoxTextColor"
    )

    val dayCellShape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Week name part on top (e.g. Sat, Sun)
        val weekdayName = weekDate.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        Text(
            text = weekdayName,
            fontSize = 15.sp,
            color = if (isFriday) Color.Red else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isFriday) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        // 2. Date number box styled exactly like MonthCalender
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 1.dp)
                .clip(dayCellShape)
                .border(
                    width = if (isToday) 1.5.dp else 0.dp,
                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = dayCellShape
                )
                .background(boxSelectedColor)
                .clickable(
                    onClick = {
                        onClick(weekDate.date)
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
                    text = weekDate.date.dayOfMonth.toString(),
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    color = boxTextColor
                )
            }
        }

        // 3. Color lines with minimum space
        if (activeEvents.isNotEmpty()) {
            val maxLane = activeEvents.mapNotNull { eventLanes[it.id] }.maxOrNull() ?: -1
            if (maxLane >= 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(1.5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (lane in 0..maxLane) {
                        val event = activeEvents.find { eventLanes[it.id] == lane }
                        if (event != null) {
                            val hasPrev = weekDate.date.isAfter(event.startDate.toLocalDate())
                            val hasNext = weekDate.date.isBefore(event.endDate.toLocalDate())

                            val leftPadding = if (hasPrev) 0.dp else 6.dp
                            val rightPadding = if (hasNext) 0.dp else 6.dp

                            val shape = RoundedCornerShape(
                                topStart = if (hasPrev) 0.dp else 1.5.dp,
                                bottomStart = if (hasPrev) 0.dp else 1.5.dp,
                                topEnd = if (hasNext) 0.dp else 1.5.dp,
                                bottomEnd = if (hasNext) 0.dp else 1.5.dp
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = leftPadding, end = rightPadding)
                                    .height(3.dp)
                                    .background(
                                        color = Color(android.graphics.Color.parseColor(event.colorHex)),
                                        shape = shape
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }
}
