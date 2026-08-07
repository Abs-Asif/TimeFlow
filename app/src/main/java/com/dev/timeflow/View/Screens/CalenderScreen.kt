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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CalendarDays
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CalenderScreen(
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val localContext = LocalContext.current
    val focusManager = LocalFocusManager.current
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

    // Lifted weekState to coordinate synchronization and jumps
    val weekState = rememberWeekCalendarState(
        startDate = startMonth.atDay(1),
        endDate = endMonth.atEndOfMonth(),
        firstVisibleWeekDate = currentSelectedDate,
        firstDayOfWeek = firstDayOfWeek
    )

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
        // Auto scroll to make sure month and week views are in sync with selected date
        state.scrollToMonth(YearMonth.from(currentSelectedDate))
        weekState.scrollToWeek(currentSelectedDate)
    }

    val tasksForDate by taskViewModel.taskForDate.collectAsState(emptyList())
    val eventsForDate by taskViewModel.eventsForDate.collectAsState(emptyList())
    val allEvents by taskViewModel.allEvents.collectAsState(emptyList())
    val currentTask by taskViewModel.currentTask.collectAsState(null)

    // Generates virtual Events representing preset unchangeable events for calendar lines
    val virtualPresetEvents = remember {
        val list = mutableListOf<Events>()
        for (year in 2000..2100) {
            // May 29: Anniversary
            val annivDate = LocalDate.of(year, 5, 29)
            val annivStart = annivDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            list.add(
                Events(
                    id = -(year * 100 + 1).toLong(),
                    name = "Our Anniversary 🎉",
                    startDate = annivStart,
                    endDate = annivDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli() - 1,
                    colorHex = "#FF00FF", // Magenta
                    createdAt = annivStart
                )
            )

            // Feb 18: Asif's Birthday
            val asifDate = LocalDate.of(year, 2, 18)
            val asifStart = asifDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            list.add(
                Events(
                    id = -(year * 100 + 2).toLong(),
                    name = "Asif's Birthday 🎉",
                    startDate = asifStart,
                    endDate = asifDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli() - 1,
                    colorHex = "#FFA500", // Orange
                    createdAt = asifStart
                )
            )

            // July 5: Monalisa's Birthday
            val monaDate = LocalDate.of(year, 7, 5)
            val monaStart = monaDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            list.add(
                Events(
                    id = -(year * 100 + 3).toLong(),
                    name = "Monalisa's Birthday 🎉",
                    startDate = monaStart,
                    endDate = monaDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli() - 1,
                    colorHex = "#008080", // Teal
                    createdAt = monaStart
                )
            )
        }
        list
    }

    // Combine database events and virtual preset events for calendar lines
    val combinedEvents = remember(allEvents, virtualPresetEvents) {
        allEvents + virtualPresetEvents
    }

    val eventLanes = remember(combinedEvents) {
        val sorted = combinedEvents.sortedWith(compareBy({ it.startDate }, { it.id }))
        val lanes = mutableMapOf<Long, Int>()
        for (event in sorted) {
            val occupiedLanes = mutableSetOf<Int>()
            for (other in sorted) {
                if (other.id == event.id) continue
                if (lanes.containsKey(other.id)) {
                    val overlap = !(event.endDate < other.startDate || event.startDate > other.endDate)
                    if (overlap) {
                        occupiedLanes.add(lanes[other.id]!!)
                    }
                }
            }
            var lane = 0
            while (lane in occupiedLanes) {
                lane++
            }
            lanes[event.id] = lane
        }
        lanes
    }
    val currentEvent by taskViewModel.currentEvent.collectAsState(null)

    val presetEvents = remember(currentSelectedDate) {
        val month = currentSelectedDate.monthValue
        val day = currentSelectedDate.dayOfMonth
        val list = mutableListOf<Pair<String, String>>()
        if (month == 5 && day == 29) {
            list.add(Pair("Our Anniversary 🎉", "#FF00FF")) // Magenta
        }
        if (month == 2 && day == 18) {
            list.add(Pair("Asif's Birthday 🎉", "#FFA500")) // Orange
        }
        if (month == 7 && day == 5) {
            list.add(Pair("Monalisa's Birthday 🎉", "#008080")) // Teal
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
    val visibleMonth = if (isWeekMode) {
        currentSelectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    } else {
        state.firstVisibleMonth.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
    val visibleYear = if (isWeekMode) {
        currentSelectedDate.year
    } else {
        state.firstVisibleMonth.yearMonth.year
    }

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
                showJumpToDatePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            val displayTitle = if (visibleYear != currentDate.year) {
                "$visibleMonth $visibleYear"
            } else {
                visibleMonth
            }
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = displayTitle,
                        label = "MonthAnimation"
                    ) { titleText ->
                        Text(
                            text = titleText,
                            modifier = Modifier.clickable {
                                showJumpToDatePicker = true
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            currentSelectedDate = currentDate
                            scope.launch {
                                state.scrollToMonth(YearMonth.from(currentDate))
                                weekState.scrollToWeek(currentDate)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.Calendar,
                            contentDescription = "Today",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
        var hasSwipedForCurrentGesture by remember { mutableStateOf(false) }
        val horizontalSwipeModifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = {
                    horizontalDrag = 0f
                    hasSwipedForCurrentGesture = false
                },
                onDragEnd = {
                    horizontalDrag = 0f
                    hasSwipedForCurrentGesture = false
                },
                onDragCancel = {
                    horizontalDrag = 0f
                    hasSwipedForCurrentGesture = false
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    if (!hasSwipedForCurrentGesture) {
                        horizontalDrag += dragAmount
                        if (horizontalDrag > 80f) {
                            currentSelectedDate = currentSelectedDate.minusDays(1)
                            hasSwipedForCurrentGesture = true
                        } else if (horizontalDrag < -80f) {
                            currentSelectedDate = currentSelectedDate.plusDays(1)
                            hasSwipedForCurrentGesture = true
                        }
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
                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                    .then(verticalSwipeModifier)
            ) {
                AnimatedContent(
                    targetState = isWeekMode,
                    label = "CalendarViewMode",
                    transitionSpec = {
                        (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) togetherWith
                         fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                    }
                ) { weekMode ->
                    if (weekMode) {
                        WeekCalendar(
                            modifier = modifier.padding(horizontal = 8.dp),
                            state = weekState,
                            dayContent = { weekDay ->
                                val cellDate = weekDay.date
                                val cellActiveEvents = combinedEvents.filter { event ->
                                    val dateStart = event.startDate.toLocalDate()
                                    val dateEnd = event.endDate.toLocalDate()
                                    !cellDate.isBefore(dateStart) && !cellDate.isAfter(dateEnd)
                                }
                                WeekCalender(
                                    selectedDate = currentSelectedDate,
                                    onClick = { currentSelectedDate = it },
                                    weekDate = weekDay,
                                    activeEvents = cellActiveEvents,
                                    eventLanes = eventLanes
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
                                val cellActiveEvents = combinedEvents.filter { event ->
                                    val dateStart = event.startDate.toLocalDate()
                                    val dateEnd = event.endDate.toLocalDate()
                                    !cellDate.isBefore(dateStart) && !cellDate.isAfter(dateEnd)
                                }
                                MonthCalender(
                                    day = day,
                                    hapticFeedback = haptics,
                                    selectedDate = currentSelectedDate,
                                    activeEvents = cellActiveEvents,
                                    eventLanes = eventLanes,
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
                                items(presetEvents) { (eventName, colorHex) ->
                                    PresetEventTile(eventName = eventName, colorHex = colorHex)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = if (WindowInsets.isImeVisible) 16.dp else 8.dp)
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Separate event adding button on the left (outside the task adder text box)
                Surface(
                    onClick = { showEventBottomSheet = true },
                    modifier = Modifier.size(44.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 3.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Lucide.CalendarDays,
                            contentDescription = "Add Event",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Compact task adder typing area
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = taskName,
                            onValueChange = { taskName = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp, end = 8.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (taskName.isNotEmpty()) {
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
                                                createdAt = currentSelectedDate.toMillis(localTime = LocalTime.now())
                                            )
                                        )
                                        taskName = ""
                                    }
                                    focusManager.clearFocus()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (taskName.isEmpty()) {
                                        Text(
                                            text = "Type your task here ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        // Separate (+) button on the right side
                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = {
                                if (taskName.isNotEmpty()) {
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
                                            createdAt = currentSelectedDate.toMillis(localTime = LocalTime.now())
                                        )
                                    )
                                    taskName = ""
                                    focusManager.clearFocus()
                                }
                            },
                            enabled = taskName.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Lucide.Plus,
                                contentDescription = "Add Task",
                                tint = if (taskName.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

val liAdorNoirritFontFamily = FontFamily(
    Font(R.font.li_ador_noirrit_regular)
)

@Composable
fun PresetEventTile(
    eventName: String,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val eventColor = Color(android.graphics.Color.parseColor(colorHex))
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.Calendar,
                    contentDescription = null,
                    tint = eventColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = eventName,
                fontFamily = liAdorNoirritFontFamily,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = eventColor
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
    val eventColor = Color(android.graphics.Color.parseColor(colorHex))
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.Calendar,
                    contentDescription = null,
                    tint = eventColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eventName,
                    fontFamily = liAdorNoirritFontFamily,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = eventColor
                    )
                )
                Text(
                    text = "$startDateStr - $endDateStr",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = eventColor.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}
