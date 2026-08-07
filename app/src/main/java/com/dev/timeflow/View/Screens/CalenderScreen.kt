package com.dev.timeflow.View.Screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ListTodo
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import com.dev.timeflow.Data.Model.ImportanceChipModel
import com.dev.timeflow.Data.Model.SavingModel
import com.dev.timeflow.Data.Model.Tasks
import com.dev.timeflow.Data.Model.Events
import com.dev.timeflow.R
import com.dev.timeflow.View.Screens.calenderScreen.MonthCalender
import com.dev.timeflow.View.Screens.calenderScreen.MonthHeader
import com.dev.timeflow.View.Screens.calenderScreen.WeekCalender
import com.dev.timeflow.View.utils.componets.SheetToAddEventAndTask
import com.dev.timeflow.View.utils.componets.SheetToEditTask
import com.dev.timeflow.View.utils.componets.SheetToAddEvent
import com.dev.timeflow.View.utils.componets.SheetToEditEvent
import com.dev.timeflow.View.utils.componets.MonthYearPickerDialog
import com.dev.timeflow.View.utils.componets.RoundedFabMenu
import com.dev.timeflow.View.utils.componets.TaskTile
import com.dev.timeflow.View.utils.endOfDayMillis
import com.dev.timeflow.View.utils.toDateTimeInMillis
import com.dev.timeflow.View.utils.toMillis
import com.dev.timeflow.View.utils.toLocalDate
import com.dev.timeflow.Viewmodel.TaskAndEventViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CalenderScreen(
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val localContext = LocalContext.current
    val taskViewModel: TaskAndEventViewModel = hiltViewModel()

    val permission = rememberPermissionState(
        permission = android.Manifest.permission.POST_NOTIFICATIONS
    )

    val isOnboardingCompleted by taskViewModel.readOnBoardingState().collectAsState(initial = true)

    LaunchedEffect(isOnboardingCompleted) {
        if (!isOnboardingCompleted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission.launchPermissionRequest()
            }
            taskViewModel.saveOnBoarding()
        }
    }
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = java.time.DayOfWeek.SATURDAY

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    val currentDate = remember { LocalDate.now() }

    // Variable to hold state of the currently selected date
    var currentSelectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    // Var to hold the switch state of the bottom sheet
    var switchState by rememberSaveable { mutableStateOf(false) }

    // Variable to hold state of the bottom sheet
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    // State for showing Event creation sheet
    var showEventBottomSheet by rememberSaveable { mutableStateOf(false) }

    // State for showing Event edit sheet
    var showEventDetails by rememberSaveable { mutableStateOf(false) }

    // State for showing Jump to Date picker
    var showJumpToDatePicker by rememberSaveable { mutableStateOf(false) }

    // State to toggle Week/Month view mode
    var isWeekMode by rememberSaveable { mutableStateOf(false) }

    // Var to hold state of the task name textfield
    var taskName by rememberSaveable { mutableStateOf("") }

    // Var to hols the timePicker
    var showTime by rememberSaveable { mutableStateOf(false) }

    // Var to hold the navigate to app info page
    var showPermissionDialog by rememberSaveable() { mutableStateOf(false) }

    // Var to hold the state of the task description textfield (hidden from UI)
    var taskDescription by rememberSaveable { mutableStateOf("") }

    var showTaskDetails by rememberSaveable { mutableStateOf(false) }

    val localTime = LocalTime.now()

    // State for the timePicker
    val timePickerState = rememberTimePickerState(
        is24Hour = false,
        initialHour = localTime.hour,
        initialMinute = localTime.minute
    )

    val scope = rememberCoroutineScope()

    LaunchedEffect(currentSelectedDate) {
        Log.d("TASKDATE", "the function ran with the updated date $currentSelectedDate")
        taskViewModel.getTasksForADate(
            start = currentSelectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            end = currentSelectedDate.endOfDayMillis()
        )
    }

    val tasksForDate by taskViewModel.taskForDate.collectAsState(emptyList())
    val eventsForDate by taskViewModel.eventsForDate.collectAsState(emptyList())
    val allEvents by taskViewModel.allEvents.collectAsState(emptyList())
    val currentTask by taskViewModel.currentTask.collectAsState(null)
    val currentEvent by taskViewModel.currentEvent.collectAsState(null)

    val presetEvents = remember(currentSelectedDate) {
        val month = currentSelectedDate.monthValue
        val day = currentSelectedDate.dayOfMonth
        val list = mutableListOf<String>()
        if (month == 5 && day == 29) {
            list.add("Our Anniversary 🎉")
        }
        if (month == 2 && day == 18) {
            list.add("Asif's Birthday 🎉")
        }
        if (month == 7 && day == 5) {
            list.add("Monalisa's Birthday 🎉")
        }
        list
    }

    val importanceChip = listOf<ImportanceChipModel>(
        ImportanceChipModel(
            label = "Low",
            color = Color(0xFF4CAF50) // Material Green 500
        ),
        ImportanceChipModel(
            label = "Medium",
            color = Color(0xFFFFC107) // Material Amber 500
        ),
        ImportanceChipModel(
            label = "High",
            color = Color(0xFFF44336) // Material Red 500
        )
    )

    // Var to hold the state of the importance chip (hidden from UI)
    var selectedChip by rememberSaveable { mutableIntStateOf(0) }

    // Dummy states required by SheetToAddEventAndTask signature (but simplified inside)
    val fromTimePickerState = rememberTimePickerState()
    val toTimePickerState = rememberTimePickerState()
    val fromDatePickerState = rememberDatePickerState()
    val toDatePickerState = rememberDatePickerState()

    val dummySavingChipList = listOf(SavingModel("Task", Lucide.ListTodo))

    // Determine the visible month and year for the TopAppBar
    val visibleMonth = state.firstVisibleMonth.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val visibleYear = state.firstVisibleMonth.yearMonth.year

    if (showPermissionDialog){
        AlertDialog(
            title = {
                Text(
                    text = "Notification Access Required"
                )
            },
            text = {
                Text(
                    text = "To ensure your reminders trigger reliably, we need access to send notifications. Please go to Settings to enable this permission"
                )
            },
            onDismissRequest = {
                showPermissionDialog = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", localContext.packageName, null)
                            addCategory(Intent.CATEGORY_DEFAULT)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        try {
                            localContext.startActivity(intent)
                        } catch (e: Exception){
                            Toast.makeText(localContext, "Some error has occurred", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text(
                        text = "Settings"
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPermissionDialog = false
                    }
                ) {
                    Text(
                        text =  "Cancel"
                    )
                }
            }
        )
    }

    if (showBottomSheet) {
        SheetToAddEventAndTask(
            onDismiss = {
                showBottomSheet = false
                taskName = ""
            },
            modifier = modifier,
            isButtonEnabled = taskName.isNotEmpty(),
            onTaskSave = {
                taskViewModel.insertTask(
                    tasks = Tasks(
                        id = 0,
                        name = taskName,
                        description = "",
                        notification = true,
                        importance = "Low",
                        taskTime = currentSelectedDate
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli(),
                        createdAt = currentSelectedDate.toMillis(localTime = localTime)
                    )
                )
            },
            onTaskNameChange = {
                taskName = it
            },
            taskName = taskName
        )
    }

    if (showEventBottomSheet) {
        SheetToAddEvent(
            initialDate = currentSelectedDate,
            onDismiss = { showEventBottomSheet = false },
            onSaveEvent = { name, start, end ->
                val color = taskViewModel.getUniqueColorForRange(
                    start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    end.endOfDayMillis()
                )
                taskViewModel.insertEvent(
                    Events(
                        name = name,
                        startDate = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        endDate = end.endOfDayMillis(),
                        colorHex = color,
                        notification = true,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        )
    }

    if (showTaskDetails && currentTask != null) {
        val latestTask = tasksForDate.find { it.id == currentTask!!.id } ?: currentTask!!

        SheetToEditTask(
            tasks = latestTask,
            onDismiss = {
                showTaskDetails = false
                taskViewModel.clearTask()
            },
            onDeleteTask = {
                taskViewModel.deleteTask(latestTask)
            },
            onSaveTask = { newName ->
                taskViewModel.updateTask(
                    latestTask.copy(name = newName)
                )
            }
        )
    }

    if (showEventDetails && currentEvent != null) {
        val latestEvent = allEvents.find { it.id == currentEvent!!.id } ?: currentEvent!!

        SheetToEditEvent(
            event = latestEvent,
            onDismiss = {
                showEventDetails = false
                taskViewModel.clearEvent()
            },
            onDeleteEvent = {
                taskViewModel.deleteEvent(latestEvent)
            },
            onSaveEvent = { newName, start, end ->
                val color = if (start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() == latestEvent.startDate &&
                    end.endOfDayMillis() == latestEvent.endDate) {
                    latestEvent.colorHex
                } else {
                    taskViewModel.getUniqueColorForRange(
                        start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        end.endOfDayMillis()
                    )
                }
                taskViewModel.updateEvent(
                    latestEvent.copy(
                        name = newName,
                        startDate = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        endDate = end.endOfDayMillis(),
                        colorHex = color
                    )
                )
            }
        )
    }

    if (showJumpToDatePicker) {
        MonthYearPickerDialog(
            initialMonth = currentSelectedDate.month,
            initialYear = currentSelectedDate.year,
            onDismiss = { showJumpToDatePicker = false },
            onConfirm = { month, year ->
                val length = java.time.YearMonth.of(year, month).lengthOfMonth()
                val targetDay = currentSelectedDate.dayOfMonth.coerceIn(1, length)
                currentSelectedDate = LocalDate.of(year, month, targetDay)
                scope.launch {
                    state.scrollToMonth(YearMonth.of(year, month))
                }
                showJumpToDatePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = visibleMonth,
                        label = "MonthAnimation"
                    ) { month ->
                        Text(
                            text = month,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                },
                actions = {
                    AnimatedContent(
                        targetState = visibleYear,
                        label = "YearAnimation",
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }
                    ) { year ->
                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = year.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            RoundedFabMenu(
                todayDate = currentDate,
                onBackToToday = {
                    currentSelectedDate = currentDate
                    scope.launch {
                        state.scrollToMonth(YearMonth.from(currentDate))
                    }
                },
                onJumpToDate = {
                    showJumpToDatePicker = true
                },
                onCreateTask = {
                    showBottomSheet = true
                },
                onCreateEvent = {
                    showEventBottomSheet = true
                }
            )
        },
    ) { innerPadding ->
        if (showTime) {
            AlertDialog(
                text = {
                    TimePicker(
                        state = timePickerState,
                    )
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showTime = false
                            switchState = false
                            timePickerState.hour = localTime.hour
                            timePickerState.minute = localTime.minute
                        }
                    ) {
                        Text(
                            "Cancel"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showTime = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                onDismissRequest = {
                    showTime = false
                    switchState = false
                    timePickerState.hour = localTime.hour
                    timePickerState.minute = localTime.minute
                },
                properties = DialogProperties(),
                title = {
                    Text(
                        text = "Pick Time"
                    )
                },
            )
        }

        // Vertical swipe gesture to shrink calendar to week view or expand to month view
        var verticalDrag by remember { mutableStateOf(0f) }
        val verticalSwipeModifier = Modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragEnd = { verticalDrag = 0f },
                onDragCancel = { verticalDrag = 0f },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    verticalDrag += dragAmount
                    if (verticalDrag < -80f) {
                        isWeekMode = true
                        verticalDrag = 0f
                    } else if (verticalDrag > 80f) {
                        isWeekMode = false
                        verticalDrag = 0f
                    }
                }
            )
        }

        // Horizontal swipe gesture below calendar to navigate days forward/backward
        var horizontalDrag by remember { mutableStateOf(0f) }
        val horizontalSwipeModifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = { horizontalDrag = 0f },
                onDragCancel = { horizontalDrag = 0f },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    horizontalDrag += dragAmount
                    if (horizontalDrag > 80f) {
                        currentSelectedDate = currentSelectedDate.minusDays(1)
                        horizontalDrag = 0f
                    } else if (horizontalDrag < -80f) {
                        currentSelectedDate = currentSelectedDate.plusDays(1)
                        horizontalDrag = 0f
                    }
                }
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(verticalSwipeModifier)
            ) {
                AnimatedContent(
                    targetState = isWeekMode,
                    label = "CalendarViewMode"
                ) { weekMode ->
                    if (weekMode) {
                        val weekState = rememberWeekCalendarState(
                            startDate = startMonth.atDay(1),
                            endDate = endMonth.atEndOfMonth(),
                            firstVisibleWeekDate = currentSelectedDate,
                            firstDayOfWeek = firstDayOfWeek
                        )
                        LaunchedEffect(currentSelectedDate) {
                            weekState.scrollToWeek(currentSelectedDate)
                        }
                        WeekCalendar(
                            modifier = modifier.padding(horizontal = 8.dp),
                            state = weekState,
                            dayContent = { weekDay ->
                                val cellDate = weekDay.date
                                val cellActiveEvents = allEvents.filter { event ->
                                    val dateStart = event.startDate.toLocalDate()
                                    val dateEnd = event.endDate.toLocalDate()
                                    !cellDate.isBefore(dateStart) && !cellDate.isAfter(dateEnd)
                                }
                                WeekCalender(
                                    selectedDate = currentSelectedDate,
                                    onClick = { currentSelectedDate = it },
                                    weekDate = weekDay,
                                    activeEvents = cellActiveEvents
                                )
                            }
                        )
                    } else {
                        HorizontalCalendar(
                            modifier = modifier.padding(horizontal = 8.dp),
                            state = state,
                            reverseLayout = false,
                            dayContent = { day ->
                                val cellDate = day.date
                                val cellActiveEvents = allEvents.filter { event ->
                                    val dateStart = event.startDate.toLocalDate()
                                    val dateEnd = event.endDate.toLocalDate()
                                    !cellDate.isBefore(dateStart) && !cellDate.isAfter(dateEnd)
                                }
                                MonthCalender(
                                    day = day,
                                    hapticFeedback = haptics,
                                    selectedDate = currentSelectedDate,
                                    activeEvents = cellActiveEvents,
                                    onClick = { date ->
                                        currentSelectedDate = date
                                    }
                                )
                            },
                            monthHeader = {
                                MonthHeader(
                                    weekName = it.weekDays.first().map {
                                        it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // Clear visible line (divider) separating the calendar and the task list
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            Column(
                modifier = modifier
                    .weight(1f)
                    .then(horizontalSwipeModifier)
            ) {
                AnimatedContent(
                    modifier = modifier
                        .align(Alignment.CenterHorizontally),
                    targetState = tasksForDate.isNotEmpty() || eventsForDate.isNotEmpty() || presetEvents.isNotEmpty()
                ) { hasContent ->
                    if (hasContent) {
                        Column(
                            modifier = modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            LazyColumn(
                                modifier = modifier.fillMaxSize()
                            ) {
                                items(presetEvents) { eventName ->
                                    PresetEventTile(eventName = eventName)
                                }
                                // Render Events ALWAYS on top of Tasks
                                items(eventsForDate) { event ->
                                    EventTile(
                                        eventName = event.name,
                                        startDateStr = event.startDate.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        endDateStr = event.endDate.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        colorHex = event.colorHex,
                                        onClick = {
                                            taskViewModel.selectEvent(event)
                                            showEventDetails = true
                                        }
                                    )
                                }
                                items(tasksForDate){ task ->
                                    TaskTile(
                                        onUpdateTask = { value ->
                                            taskViewModel.updateTask(
                                                tasks = task.copy(
                                                    isCompleted = value
                                                )
                                            )
                                        },
                                        taskName = task.name,
                                        taskDescription = task.description,
                                        taskIsCompleted = task.isCompleted,
                                        taskImportance = task.importance,
                                        taskNotification = task.notification,
                                        taskTime = task.taskTime!!,
                                        onClick = {
                                            taskViewModel.selectTask(tasks = task)
                                            showTaskDetails = true
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                modifier = modifier.size(150.dp),
                                model = R.drawable.emptytask,
                                contentDescription = null
                            )
                            Text(
                                text = "Chill out buddy\nyou got nothing"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetEventTile(
    eventName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.Calendar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = eventName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

@Composable
fun EventTile(
    modifier: Modifier = Modifier,
    eventName: String,
    startDateStr: String,
    endDateStr: String,
    colorHex: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        color = Color(android.graphics.Color.parseColor(colorHex)).copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(android.graphics.Color.parseColor(colorHex))
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.Calendar,
                contentDescription = null,
                tint = Color(android.graphics.Color.parseColor(colorHex)),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eventName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "$startDateStr - $endDateStr",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}
