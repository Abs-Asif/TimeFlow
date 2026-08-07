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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.composables.icons.lucide.ListTodo
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.dev.timeflow.Data.Model.ImportanceChipModel
import com.dev.timeflow.Data.Model.SavingModel
import com.dev.timeflow.Data.Model.Tasks
import com.dev.timeflow.R
import com.dev.timeflow.View.Screens.calenderScreen.MonthCalender
import com.dev.timeflow.View.Screens.calenderScreen.MonthHeader
import com.dev.timeflow.View.utils.componets.SheetToAddEventAndTask
import com.dev.timeflow.View.utils.componets.SheetToEditTask
import com.dev.timeflow.View.utils.componets.TaskTile
import com.dev.timeflow.View.utils.endOfDayMillis
import com.dev.timeflow.View.utils.toDateTimeInMillis
import com.dev.timeflow.View.utils.toMillis
import com.dev.timeflow.Viewmodel.TaskAndEventViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalenderScreen(
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val localContext = LocalContext.current
    val taskViewModel: TaskAndEventViewModel = hiltViewModel()
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
                        description = "",
                        notification = switchState,
                        importance = "Low",
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

        var editedDescription by remember(latestTask.id) { mutableStateOf(latestTask.description ?: "") }
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

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        weekName = it.weekDays.first().map {
                            it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        }
                    )
                }
            )

            // Clear visible line (divider) separating the calendar and the task list
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

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
