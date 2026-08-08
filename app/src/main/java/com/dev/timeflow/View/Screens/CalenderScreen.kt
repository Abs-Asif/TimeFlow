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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.X
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val haptics = LocalHapticFeedback.current
    val localContext = LocalContext.current
    val focusManager = LocalFocusManager.current
    val taskViewModel: TaskAndEventViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

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
    val startMonth = remember { YearMonth.of(1900, 1) }
    val endMonth = remember { YearMonth.of(2200, 12) }
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

    // State for Search
    var showSearchMenu by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val searchOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val searchDragModifier = Modifier.pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragEnd = {
                if (searchOffsetY.value > 300f) {
                    showSearchMenu = false
                    searchQuery = ""
                    scope.launch { searchOffsetY.snapTo(0f) }
                } else {
                    scope.launch {
                        searchOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                    }
                }
            },
            onDragCancel = {
                scope.launch {
                    searchOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                }
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                scope.launch {
                    searchOffsetY.snapTo((searchOffsetY.value + dragAmount).coerceAtLeast(0f))
                }
            }
        )
    }

    LaunchedEffect(showSearchMenu) {
        if (!showSearchMenu) {
            searchOffsetY.snapTo(0f)
        }
    }

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

    LaunchedEffect(currentSelectedDate) {
        Log.d("TASKDATE", "the function ran with the updated date $currentSelectedDate")
        taskViewModel.getTasksForADate(
            start = currentSelectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            end = currentSelectedDate.endOfDayMillis()
        )
        // Auto scroll only the active view to optimize performance
        if (!isWeekMode) {
            if (state.firstVisibleMonth.yearMonth != YearMonth.from(currentSelectedDate)) {
                state.scrollToMonth(YearMonth.from(currentSelectedDate))
            }
        } else {
            weekState.scrollToWeek(currentSelectedDate)
        }
    }

    LaunchedEffect(isWeekMode) {
        if (isWeekMode) {
            weekState.scrollToWeek(currentSelectedDate)
        } else {
            state.scrollToMonth(YearMonth.from(currentSelectedDate))
        }
    }

    val tasksForDate by taskViewModel.taskForDate.collectAsState(emptyList())
    val eventsForDate by taskViewModel.eventsForDate.collectAsState(emptyList())
    val allEvents by taskViewModel.allEvents.collectAsState(emptyList())
    val allTasks by taskViewModel.allTasks.collectAsState(emptyList())
    val currentTask by taskViewModel.currentTask.collectAsState(null)

    // Generates virtual Events representing preset unchangeable events for calendar lines
    val virtualPresetEvents = remember {
        val list = mutableListOf<Events>()
        val presetTemplates = listOf(
            PresetEventTemplate("আমাদের বিবাহ বার্ষিকী 🎉", 5, 29, "#E91E63"),
            PresetEventTemplate("আসিফের জন্মদিন 🎉", 2, 18, "#FFA500"),
            PresetEventTemplate("মোনালিসার জন্মদিন 🎉", 7, 5, "#008080"),
            PresetEventTemplate("আদনানের জন্মদিন 🎉", 11, 2, "#FFA500"),
            PresetEventTemplate("মাহিরার জন্মদিন 🎉", 11, 23, "#FFA500")
        )
        for (year in 2000..2100) {
            presetTemplates.forEachIndexed { index, template ->
                val date = LocalDate.of(year, template.month, template.day)
                val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                list.add(
                    Events(
                        id = -(year * 100 + index + 1).toLong(),
                        name = template.name,
                        startDate = start,
                        endDate = date.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli() - 1,
                        colorHex = template.colorHex,
                        createdAt = start
                    )
                )
            }
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

    val searchResults = remember(allTasks, allEvents, searchQuery) {
        val query = searchQuery.trim()
        if (query.length < 2) {
            emptyList<SearchResult>()
        } else {
            val taskList = allTasks.filter { it.name.contains(query, ignoreCase = true) }
                .map { SearchResult.TaskResult(it) }

            val eventList = allEvents.filter { it.name.contains(query, ignoreCase = true) }
                .map { SearchResult.EventResult(it) }

            val presetTemplates = listOf(
                PresetEventTemplate("আমাদের বিবাহ বার্ষিকী 🎉", 5, 29, "#E91E63"),
                PresetEventTemplate("আসিফের জন্মদিন 🎉", 2, 18, "#FFA500"),
                PresetEventTemplate("মোনালিসার জন্মদিন 🎉", 7, 5, "#008080"),
                PresetEventTemplate("আদনানের জন্মদিন 🎉", 11, 2, "#FFA500"),
                PresetEventTemplate("মাহিরার জন্মদিন 🎉", 11, 23, "#FFA500")
            )
            val matchedPresets = presetTemplates.filter { it.name.contains(query, ignoreCase = true) }
                .mapIndexed { index, template ->
                    SearchResult.PresetEventResult(
                        id = -(index + 1).toLong(),
                        name = template.name,
                        month = template.month,
                        day = template.day,
                        colorHex = template.colorHex
                    )
                }

            (taskList + eventList + matchedPresets).sortedBy { it.epochMillis }
        }
    }

    val presetEvents = remember(currentSelectedDate) {
        val month = currentSelectedDate.monthValue
        val day = currentSelectedDate.dayOfMonth
        val templates = listOf(
            PresetEventTemplate("আমাদের বিবাহ বার্ষিকী 🎉", 5, 29, "#E91E63"),
            PresetEventTemplate("আসিফের জন্মদিন 🎉", 2, 18, "#FFA500"),
            PresetEventTemplate("মোনালিসার জন্মদিন 🎉", 7, 5, "#008080"),
            PresetEventTemplate("আদনানের জন্মদিন 🎉", 11, 2, "#FFA500"),
            PresetEventTemplate("মাহিরার জন্মদিন 🎉", 11, 23, "#FFA500")
        )
        templates.filter { it.month == month && it.day == day }
            .map { Pair(it.name, it.colorHex) }
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                            modifier = Modifier.clickable(enabled = !showSearchMenu) {
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
                        onClick = {
                            if (!showSearchMenu) {
                                showSearchMenu = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            if (!showSearchMenu) {
                                currentSelectedDate = currentDate
                                scope.launch {
                                    state.scrollToMonth(YearMonth.from(currentDate))
                                    weekState.scrollToWeek(currentDate)
                                }
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
        val verticalSwipeModifier = if (showSearchMenu) Modifier else Modifier.pointerInput(Unit) {
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
        val horizontalSwipeModifier = if (showSearchMenu) Modifier else Modifier.pointerInput(Unit) {
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

        if (isLandscape) {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Left side: Calendar with headers + Search Swipe up menu covering only this left side
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
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
                                                onClick = { if (!showSearchMenu) currentSelectedDate = it },
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
                                                    if (!showSearchMenu) {
                                                        currentSelectedDate = date
                                                    }
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
                    }

                }

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                // Right side: prayer tracker, tasks & events list, with adding features
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PrayerTrackerRow(
                        date = currentSelectedDate,
                        viewModel = taskViewModel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(horizontalSwipeModifier)
                    ) {
                        AnimatedContent(
                            targetState = currentSelectedDate,
                            transitionSpec = {
                                if (targetState.isAfter(initialState)) {
                                    (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> -width } + fadeOut())
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> width } + fadeOut())
                                }
                            },
                            label = "DateTransition"
                        ) { targetDate ->
                            val tasksForTargetDate = remember(allTasks, targetDate) {
                                allTasks.filter { task ->
                                    val taskDate = task.taskTime?.toLocalDate() ?: task.createdAt.toLocalDate()
                                    taskDate == targetDate
                                }
                            }

                            val eventsForTargetDate = remember(allEvents, targetDate) {
                                allEvents.filter { event ->
                                    val dateStart = event.startDate.toLocalDate()
                                    val dateEnd = event.endDate.toLocalDate()
                                    !targetDate.isBefore(dateStart) && !targetDate.isAfter(dateEnd)
                                }
                            }

                            val presetEventsForTargetDate = remember(targetDate) {
                                val month = targetDate.monthValue
                                val day = targetDate.dayOfMonth
                                val templates = listOf(
                                    PresetEventTemplate("আমাদের বিবাহ বার্ষিকী 🎉", 5, 29, "#E91E63"),
                                    PresetEventTemplate("আসিফের জন্মদিন 🎉", 2, 18, "#FFA500"),
                                    PresetEventTemplate("মোনালিসার জন্মদিন 🎉", 7, 5, "#008080"),
                                    PresetEventTemplate("আদনানের জন্মদিন 🎉", 11, 2, "#FFA500"),
                                    PresetEventTemplate("মাহিরার জন্মদিন 🎉", 11, 23, "#FFA500")
                                )
                                templates.filter { it.month == month && it.day == day }
                                    .map { Pair(it.name, it.colorHex) }
                            }

                            val hasContent = tasksForTargetDate.isNotEmpty() || eventsForTargetDate.isNotEmpty() || presetEventsForTargetDate.isNotEmpty()

                            if (hasContent) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(presetEventsForTargetDate) { (eventName, colorHex) ->
                                            PresetEventTile(eventName = eventName, colorHex = colorHex)
                                        }
                                        items(eventsForTargetDate) { event ->
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
                                        items(tasksForTargetDate) { task ->
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
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    AsyncImage(
                                        modifier = Modifier.size(100.dp),
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
                            .padding(top = 4.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = { showEventBottomSheet = true },
                            modifier = Modifier.size(44.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 0.dp
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

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(22.dp),
                            shadowElevation = 0.dp
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
                                        imeAction = ImeAction.Send
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
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
                                        }
                                    },
                                    enabled = taskName.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Lucide.Send,
                                        contentDescription = "Send Task",
                                        tint = if (taskName.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
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
                                        onClick = { if (!showSearchMenu) currentSelectedDate = it },
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
                                            if (!showSearchMenu) {
                                                currentSelectedDate = date
                                            }
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
                        .padding(top = 12.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                // Prayer Tracker Row
                PrayerTrackerRow(
                    date = currentSelectedDate,
                    viewModel = taskViewModel
                )

                // Final separating line
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                Column(
                    modifier = modifier
                        .weight(1f)
                        .then(horizontalSwipeModifier)
                ) {
                    AnimatedContent(
                        targetState = currentSelectedDate,
                        transitionSpec = {
                            if (targetState.isAfter(initialState)) {
                                (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                            }
                        },
                        label = "DateTransition"
                    ) { targetDate ->
                        val tasksForTargetDate = remember(allTasks, targetDate) {
                            allTasks.filter { task ->
                                val taskDate = task.taskTime?.toLocalDate() ?: task.createdAt.toLocalDate()
                                taskDate == targetDate
                            }
                        }

                        val eventsForTargetDate = remember(allEvents, targetDate) {
                            allEvents.filter { event ->
                                val dateStart = event.startDate.toLocalDate()
                                val dateEnd = event.endDate.toLocalDate()
                                !targetDate.isBefore(dateStart) && !targetDate.isAfter(dateEnd)
                            }
                        }

                        val presetEventsForTargetDate = remember(targetDate) {
                            val month = targetDate.monthValue
                            val day = targetDate.dayOfMonth
                            val templates = listOf(
                                PresetEventTemplate("আমাদের বিবাহ বার্ষিকী 🎉", 5, 29, "#E91E63"),
                                PresetEventTemplate("আসিফের জন্মদিন 🎉", 2, 18, "#FFA500"),
                                PresetEventTemplate("মোনালিসার জন্মদিন 🎉", 7, 5, "#008080"),
                                PresetEventTemplate("আদনানের জন্মদিন 🎉", 11, 2, "#FFA500"),
                                PresetEventTemplate("মাহিরার জন্মদিন 🎉", 11, 23, "#FFA500")
                            )
                            templates.filter { it.month == month && it.day == day }
                                .map { Pair(it.name, it.colorHex) }
                        }

                        val hasContent = tasksForTargetDate.isNotEmpty() || eventsForTargetDate.isNotEmpty() || presetEventsForTargetDate.isNotEmpty()

                        if (hasContent) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(presetEventsForTargetDate) { (eventName, colorHex) ->
                                        PresetEventTile(eventName = eventName, colorHex = colorHex)
                                    }
                                    // Render Events ALWAYS on top of Tasks
                                    items(eventsForTargetDate) { event ->
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
                                    items(tasksForTargetDate) { task ->
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
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AsyncImage(
                                    modifier = Modifier.size(150.dp),
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
                        .padding(top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Separate event adding button on the left (outside the task adder text box)
                    Surface(
                        onClick = { showEventBottomSheet = true },
                        modifier = Modifier.size(44.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 0.dp
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
                        shadowElevation = 0.dp
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
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
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

                            // Separate paper plane (send) button on the right side
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
                                    }
                                },
                                enabled = taskName.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Lucide.Send,
                                    contentDescription = "Send Task",
                                    tint = if (taskName.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Slide-up Overlay (always on top of everything, covering the full screen or left side in landscape)
        AnimatedVisibility(
            visible = showSearchMenu,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left side: Search Overlay covering left half (weight 1.1)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1.1f)
                    ) {
                        SearchOverlay(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            searchResults = searchResults,
                            onClose = { showSearchMenu = false },
                            onResultClick = { date ->
                                currentSelectedDate = date
                                showSearchMenu = false
                                searchQuery = ""
                            },
                            searchOffsetY = searchOffsetY.value,
                            searchDragModifier = searchDragModifier
                        )
                    }
                    // Right side: empty spacer/scrim that is clickable to close the search overlay (weight 0.9)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.9f)
                            .clickable(
                                onClick = { showSearchMenu = false },
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            )
                    )
                }
            } else {
                SearchOverlay(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    searchResults = searchResults,
                    onClose = { showSearchMenu = false },
                    onResultClick = { date ->
                        currentSelectedDate = date
                        showSearchMenu = false
                        searchQuery = ""
                    },
                    searchOffsetY = searchOffsetY.value,
                    searchDragModifier = searchDragModifier
                )
            }
        }
    }
}
}

@Composable
fun SearchOverlay(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<SearchResult>,
    onClose: () -> Unit,
    onResultClick: (LocalDate) -> Unit,
    searchOffsetY: Float,
    searchDragModifier: Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationY = searchOffsetY }
            .statusBarsPadding()
            .padding(top = 64.dp)
            .background(Color.Black) // AMOLED black background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(searchDragModifier)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 4.dp)
                            .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
                }

                // Compact Search Bar Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(44.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxSize()) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search tasks and events...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotEmpty()) {
                                onSearchQueryChange("")
                            } else {
                                onClose()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.X,
                            contentDescription = "Clear or Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (searchQuery.trim().length >= 2) {
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp) // compact gaps
                    ) {
                        items(searchResults) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onResultClick(result.date)
                                    }
                                    .padding(vertical = 6.dp), // compact padding
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (result) {
                                    is SearchResult.TaskResult -> Lucide.ListTodo
                                    is SearchResult.EventResult -> {
                                        if (result.name.contains("বিবাহ বার্ষিকী")) Lucide.Heart else Lucide.Calendar
                                    }
                                    is SearchResult.PresetEventResult -> {
                                        if (result.name.contains("বিবাহ বার্ষিকী")) Lucide.Heart else Lucide.Calendar
                                    }
                                }
                                val color = when (result) {
                                    is SearchResult.TaskResult -> MaterialTheme.colorScheme.primary
                                    is SearchResult.EventResult -> {
                                        val hex = result.event.colorHex
                                        if (hex.isNotEmpty()) Color(android.graphics.Color.parseColor(hex)) else MaterialTheme.colorScheme.secondary
                                    }
                                    is SearchResult.PresetEventResult -> {
                                        Color(android.graphics.Color.parseColor(result.colorHex))
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp) // compact icon size
                                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = result.name,
                                        fontFamily = liAdorNoirritFontFamily,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    val dateText = if (result is SearchResult.PresetEventResult) {
                                        val formatter = DateTimeFormatter.ofPattern("dd MMMM")
                                        "Each year on ${result.date.format(formatter)}"
                                    } else {
                                        result.date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
                                    }
                                    Text(
                                        text = dateText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Type at least 2 letters to search",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

sealed class SearchResult {
    abstract val id: Long
    abstract val name: String
    abstract val date: LocalDate
    abstract val epochMillis: Long

    data class TaskResult(
        val task: Tasks,
        override val id: Long = task.id,
        override val name: String = task.name,
        override val date: LocalDate = (task.taskTime ?: task.createdAt).toLocalDate(),
        override val epochMillis: Long = task.taskTime ?: task.createdAt
    ) : SearchResult()

    data class EventResult(
        val event: Events,
        override val id: Long = event.id,
        override val name: String = event.name,
        override val date: LocalDate = event.startDate.toLocalDate(),
        override val epochMillis: Long = event.startDate
    ) : SearchResult()

    data class PresetEventResult(
        override val id: Long,
        override val name: String,
        val month: Int,
        val day: Int,
        val colorHex: String,
        override val date: LocalDate = LocalDate.of(LocalDate.now().year, month, day),
        override val epochMillis: Long = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    ) : SearchResult()
}

data class PresetEventTemplate(
    val name: String,
    val month: Int,
    val day: Int,
    val colorHex: String
)

val liAdorNoirritFontFamily = FontFamily(
    Font(R.font.li_ador_noirrit_regular)
)

@Composable
fun PrayerTrackerRow(
    date: java.time.LocalDate,
    viewModel: com.dev.timeflow.Viewmodel.TaskAndEventViewModel,
    modifier: Modifier = Modifier
) {
    val isFriday = date.dayOfWeek == java.time.DayOfWeek.FRIDAY
    val prayers = if (isFriday) {
        listOf("ফজর", "জুমআ", "আসর", "মাগরিব", "এশা")
    } else {
        listOf("ফজর", "যোহর", "আসর", "মাগরিব", "এশা")
    }
    val checkedPrayers by viewModel.getCheckedPrayers(date.toString()).collectAsState(initial = emptySet())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        prayers.forEach { prayer ->
            val isChecked = prayer in checkedPrayers
            val backgroundColor by animateColorAsState(
                targetValue = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                label = "PrayerBg"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isChecked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                label = "PrayerContent"
            )

            val isFuture = date.isAfter(java.time.LocalDate.now())
            if (isFuture) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(50), // semi circular-rectangular (pill)
                    color = backgroundColor,
                    contentColor = contentColor,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Lucide.Check,
                                contentDescription = "Checked",
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = prayer,
                                fontFamily = liAdorNoirritFontFamily,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            } else {
                Surface(
                    onClick = {
                        viewModel.togglePrayer(date.toString(), prayer, !isChecked)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(50), // semi circular-rectangular (pill)
                    color = backgroundColor,
                    contentColor = contentColor,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Lucide.Check,
                                contentDescription = "Checked",
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = prayer,
                                fontFamily = liAdorNoirritFontFamily,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp
                                )
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
                    imageVector = if (eventName.contains("বিবাহ বার্ষিকী") || eventName.contains("Anniversary")) Lucide.Heart else Lucide.Calendar,
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
