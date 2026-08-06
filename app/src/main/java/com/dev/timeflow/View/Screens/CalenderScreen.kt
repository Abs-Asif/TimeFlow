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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
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

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.window.DialogProperties

import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ListTodo
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.dev.timeflow.Data.Model.ImportanceChipModel
import com.dev.timeflow.Data.Model.SavingModel
import com.dev.timeflow.Data.Model.Tasks
import com.dev.timeflow.View.utils.componets.TaskTile
import com.dev.timeflow.View.utils.endOfDayMillis
import com.dev.timeflow.View.utils.toDateTimeInMillis
import com.dev.timeflow.View.utils.toMillis
import com.dev.timeflow.Viewmodel.TaskAndEventViewModel
import com.dev.timeflow.R
import com.dev.timeflow.View.Screens.calenderScreen.MonthCalender
import com.dev.timeflow.View.Screens.calenderScreen.MonthHeader
import com.dev.timeflow.View.Screens.calenderScreen.WeekCalender
import com.dev.timeflow.View.utils.componets.SheetToAddEventAndTask
import com.dev.timeflow.View.utils.componets.SheetToEditTask
import com.dev.timeflow.View.utils.toUtcMillis
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalenderScreen(
    modifier: Modifier = Modifier,
    selectedTab : Int
) {
    val haptics = LocalHapticFeedback.current
    val localContext = LocalContext.current
    val taskViewModel: TaskAndEventViewModel = hiltViewModel()
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    val currentDate = remember { LocalDate.now() }

    val startDate = remember { currentMonth.minusMonths(100).atStartOfMonth() }
    val endDate = remember { currentMonth.plusMonths(100).atEndOfMonth() }
    val weekState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = currentDate,
        firstDayOfWeek = firstDayOfWeek
    )

    //variable to hold state of the currently selected date
    var currentSelectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    // var to hold the switch state of the bottom sheet
    var switchState by rememberSaveable { mutableStateOf(false) }

    // variable to hold state of the bottom sheet
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    // var to hold state of the task name textfield
    var taskName by rememberSaveable { mutableStateOf("") }

    // var to hols the timePicker
    var showTime by rememberSaveable { mutableStateOf(false) }

    // var to hold the navigate to app info page
    var showPermissionDialog by rememberSaveable() {mutableStateOf(false) }

    // var to hold the state of the task description textfield
    var taskDescription by rememberSaveable { mutableStateOf("") }

    var showTaskDetails by rememberSaveable { mutableStateOf(false) }

    val localTime = LocalTime.now()

    // state for the timePicker
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
    val currentTask by taskViewModel.currentTask.collectAsState(null)
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

    // var to hold the state of the importance chip
    var selectedChip by rememberSaveable { mutableIntStateOf(0) }

    // Dummy states required by SheetToAddEventAndTask signature (but simplified inside)
    val fromTimePickerState = rememberTimePickerState()
    val toTimePickerState = rememberTimePickerState()
    val fromDatePickerState = rememberDatePickerState()
    val toDatePickerState = rememberDatePickerState()

    val dummySavingChipList = listOf(SavingModel("Task", Lucide.ListTodo))

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
                            data = Uri.fromParts("package",localContext.packageName, null)
                            addCategory(Intent.CATEGORY_DEFAULT)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        try {
                            localContext.startActivity(intent)
                        }catch (e: Exception){
                            Toast.makeText(localContext,"Some error has occurred", Toast.LENGTH_LONG).show()
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
                taskDescription = ""
                switchState = false
                timePickerState.hour = localTime.hour
                timePickerState.minute = localTime.minute
            },
            modifier = modifier,
            onSwitchState = {
                switchState = it
            },
            selectedSavingType = 0,
            isButtonEnabled = taskName.isNotEmpty(),
            onTaskSave = {
                taskViewModel.insertTask(
                    tasks = Tasks(
                        id = 0,
                        name = taskName,
                        description = taskDescription,
                        notification = switchState,
                        importance = importanceChip[selectedChip].label,
                        taskTime = if (switchState) Calendar.getInstance().toDateTimeInMillis(
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                            date = currentSelectedDate
                        ) else {
                            0
                        },
                        createdAt = currentSelectedDate.toMillis(localTime = localTime)
                    )
                )
                switchState = false
                timePickerState.hour = localTime.hour
                timePickerState.minute = localTime.minute
            },
            onEventSave = {},
            onSelectedImportantChipChange = {
                selectedChip = it
            },
            onTaskNameChange = {
                taskName = it
            },
            onTaskDescriptionChange = {
                taskDescription = it
            },
            changeSavingType = {},
            savingChipList = dummySavingChipList,
            importanceChip = importanceChip,
            hapticFeedback = haptics,
            switchState = switchState,
            taskName = taskName,
            taskDescription = taskDescription,
            selectedImportantChip = selectedChip,
            showTimeState = showTime,
            onTimeState = {
                showTime = it
            },
            timerState = timePickerState,
            onPermissionState = {
                showPermissionDialog = it
            },
            onFromTileClick = {},
            onToTileClick = {},
            fromTimePickerState = fromTimePickerState,
            toTimePickerState = toTimePickerState,
            fromDatePickerState = fromDatePickerState,
            toDatePickerState = toDatePickerState,
            onFromTimePicker = {},
            onToTimePicker = {}
        )
    }

    if (showTaskDetails && currentTask != null) {
        val latestTask = tasksForDate.find { it.id == currentTask!!.id } ?: currentTask!!

        var editedDescription by remember(latestTask.id) { mutableStateOf(latestTask.description) }
        var editedName by remember(latestTask.id) { mutableStateOf(latestTask.name) }

        SheetToEditTask(
            tasks = latestTask,
            onDismiss = {
                showTaskDetails = false
                taskViewModel.clearTask()
            },
            onCheckBoxValueChange = {
                taskViewModel.updateTask(
                    latestTask.copy(
                        isCompleted = it
                    )
                )
            },
            onValueChange = {
                editedDescription = it
            },
            onNameValueChange = {
                editedName = it
            },
            onDeleteTask = {
                taskViewModel.deleteTask(
                    latestTask
                )
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = !showBottomSheet
                }
            ) {
                Icon(imageVector = Lucide.Plus, contentDescription = null)
            }
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
                            switchState =false
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

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                modifier = modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                targetState = selectedTab,
                transitionSpec = {
                    scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn() togetherWith scaleOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut()
                }
            ) {
                if (it == 0){
                    WeekCalendar(
                        modifier = modifier.padding(
                            horizontal = 16.dp
                        ),
                        state = weekState,
                        dayContent = { weekDate ->
                            WeekCalender(
                                weekDate = weekDate,
                                selectedDate = currentSelectedDate,
                                onClick = {
                                    currentSelectedDate = it
                                }
                            )
                        },
                    )
                } else if(it == 1){
                    HorizontalCalendar(
                        modifier = modifier.padding(
                            horizontal = 8.dp
                        ),
                        state = state,
                        reverseLayout = false,
                        dayContent = {
                            MonthCalender(
                                day = it,
                                hapticFeedback = haptics,
                                selectedDate = currentSelectedDate,
                                onClick = { date ->
                                    currentSelectedDate = date
                                }
                            )
                        },
                        monthHeader = {
                            MonthHeader(
                                monthName = it.yearMonth.month.toString(),
                                weekName = it.weekDays.first().map {
                                    it.date.dayOfWeek.toString().take(3).lowercase()
                                        .replaceFirstChar {
                                            it.uppercase()
                                        }
                                },
                                onClick = {
                                    scope.launch {
                                        state.animateScrollToMonth(currentMonth)
                                    }
                                }
                            )
                        }
                    )
                }
            }

            Column(
                modifier = modifier.weight(1f)
            ) {
                AnimatedContent(
                    modifier = modifier
                        .align(Alignment.CenterHorizontally),
                    targetState = tasksForDate.isNotEmpty()
                ) { hasTasks ->
                    if (hasTasks) {
                        Column(
                            modifier = modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            LazyColumn(
                                modifier = modifier.fillMaxSize()
                            ) {
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
