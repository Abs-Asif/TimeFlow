package com.dev.timeflow.Viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.timeflow.Data.Model.NotificationAlarmManagerModel
import com.dev.timeflow.Data.Model.Tasks
import com.dev.timeflow.Data.Model.Events
import com.dev.timeflow.Data.Repo.DataStoreRepo
import com.dev.timeflow.Data.Repo.TaskRepo
import com.dev.timeflow.Data.Repo.EventRepo
import com.dev.timeflow.Managers.notification.TimeFlowAlarmManagerService
import com.dev.timeflow.View.Navigation.Routes
import com.dev.timeflow.View.utils.toHour
import com.dev.timeflow.View.utils.toLocalDate
import com.dev.timeflow.View.utils.toMinute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskAndEventViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreRepo: DataStoreRepo,
    private val taskRepo: TaskRepo,
    private val eventRepo: EventRepo
) : ViewModel(){

    private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _startDestination: MutableState<String> = mutableStateOf(Routes.WelcomeScreen.route)
    var startDestination : State<String> = _startDestination

    private val  _isCompleted : MutableState<Boolean> = mutableStateOf(false)
    val isCompleted : State<Boolean> = _isCompleted
    init {
        viewModelScope.launch {
           val i = readOnBoardingState().first()
            _isCompleted.value = i
            if (i){
                _startDestination.value = Routes.CalendarScreen.route
            }else{
                _startDestination.value = Routes.WelcomeScreen.route
            }
            delay(500)
            _isLoading.value = false

            getAllTasks()
            getAllEvents()
        }
    }

    // job to track and cancel the task fetch operation
    private var taskJob: Job? = null
    private var eventJob: Job? = null

    private val _currentTask = MutableStateFlow<Tasks?>(null)
    var currentTask = _currentTask

    // select task
    fun selectTask (tasks: Tasks){
        _currentTask.value = tasks
    }

    fun clearTask (){
        _currentTask.value = null
    }

    private val _currentEvent = MutableStateFlow<Events?>(null)
    var currentEvent = _currentEvent

    fun selectEvent (events: Events){
        _currentEvent.value = events
    }

    fun clearEvent (){
        _currentEvent.value = null
    }


    var scrollStateValue = MutableStateFlow<Int>(0)

    fun manageScrollState(scrollValue: Int){
        scrollStateValue.value = scrollValue
    }


    // variable to hold the all the tasks in the database
    private val _allTasks = MutableStateFlow<List<Tasks>>(emptyList())
    var allTasks : StateFlow<List<Tasks>> = _allTasks

    fun getAllTasks() {
        viewModelScope.launch {
            val tasks = taskRepo.getAllTasks()
            tasks.collect {
                _allTasks.value = it
            }
        }
    }

    // variable to hold all the events in the database
    private val _allEvents = MutableStateFlow<List<Events>>(emptyList())
    var allEvents : StateFlow<List<Events>> = _allEvents

    fun getAllEvents() {
        viewModelScope.launch {
            eventRepo.getAllEvents().collect {
                _allEvents.value = it
            }
        }
    }
    // function to add a task to the database
    fun insertTask(tasks: Tasks){
        viewModelScope.launch(Dispatchers.IO) {
            val generatedId = taskRepo.insertTask(
                tasks = tasks
            )
            if (tasks.notification) {
                val taskDate = tasks.taskTime?.toLocalDate() ?: tasks.createdAt.toLocalDate()

                // 1. Alarm for 12:00 AM (hour = 0, minute = 0)
                scheduleNotification(
                    notificationAlarmManagerModel = NotificationAlarmManagerModel(
                        id = generatedId,
                        type = 1,
                        hour = 0,
                        minute = 0,
                        title = tasks.name,
                        localDate = taskDate
                    )
                )

                // 2. Alarm for 7:00 AM (hour = 7, minute = 0)
                scheduleNotification(
                    notificationAlarmManagerModel = NotificationAlarmManagerModel(
                        id = generatedId,
                        type = 1,
                        hour = 7,
                        minute = 0,
                        title = tasks.name,
                        localDate = taskDate
                    )
                )
            }
        }
    }

    // function to update a task in the database
    fun updateTask(tasks: Tasks){
        viewModelScope.launch {
            taskRepo.updateTask(
                tasks = tasks
            )
            if (tasks.notification) {
                val taskDate = tasks.taskTime?.toLocalDate() ?: tasks.createdAt.toLocalDate()

                // 1. Alarm for 12:00 AM (hour = 0, minute = 0)
                scheduleNotification(
                    notificationAlarmManagerModel = NotificationAlarmManagerModel(
                        id = tasks.id,
                        type = 1,
                        hour = 0,
                        minute = 0,
                        title = tasks.name,
                        localDate = taskDate
                    )
                )

                // 2. Alarm for 7:00 AM (hour = 7, minute = 0)
                scheduleNotification(
                    notificationAlarmManagerModel = NotificationAlarmManagerModel(
                        id = tasks.id,
                        type = 1,
                        hour = 7,
                        minute = 0,
                        title = tasks.name,
                        localDate = taskDate
                    )
                )
            }
        }
    }


    // variable to hold the task for tasks
    private var  _taskForDate = MutableStateFlow<List<Tasks>>(emptyList())
    var taskForDate : StateFlow<List<Tasks>> = _taskForDate

    private var _eventsForDate = MutableStateFlow<List<Events>>(emptyList())
    var eventsForDate : StateFlow<List<Events>> = _eventsForDate

    // function to get tasks for a date
    fun getTasksForADate(start : Long, end : Long) {

        //cancelling existing task fetch
        taskJob?.cancel()
        eventJob?.cancel()

        _taskForDate.value = emptyList()
        _eventsForDate.value = emptyList()

        taskJob =  viewModelScope.launch {
            taskRepo.getTasksForADate(
              start = start, end = end
            ).collect {
                _taskForDate.value = it
            }
        }

        eventJob = viewModelScope.launch {
            eventRepo.getEventsForRange(
                start = start, end = end
            ).collect {
                _eventsForDate.value = it
            }
        }
    }

    private var _taskForToday = MutableStateFlow<List<Tasks>>(emptyList())
    var taskForToday : StateFlow<List<Tasks>> = _taskForToday

    fun getTasksForToday (start: Long, end: Long){
        viewModelScope.launch {
            taskRepo.getTasksForADate(
               start = start,
                end = end
            ).collect {
                _taskForToday.value = it
            }
        }
    }

    // function to delete a task from the database
    fun deleteTask(tasks: Tasks){
       viewModelScope.launch(Dispatchers.IO) {
           taskRepo.deleteTask(
               tasks = tasks
           )
       }
    }

    fun insertEvent(event: Events){
        viewModelScope.launch(Dispatchers.IO) {
            val generatedId = eventRepo.insertEvent(event)
            if (event.notification) {
                val eventDate = event.startDate.toLocalDate()

                // 1. Alarm for 12:00 AM
                scheduleNotification(
                    notificationAlarmManagerModel = NotificationAlarmManagerModel(
                        id = generatedId,
                        type = 0,
                        startTime = event.startDate,
                        endTime = event.endDate,
                        hour = 0,
                        minute = 0,
                        title = event.name,
                        localDate = eventDate
                    )
                )

                // 2. Alarm for 7:00 AM
                scheduleNotification(
                    notificationAlarmManagerModel = NotificationAlarmManagerModel(
                        id = generatedId,
                        type = 0,
                        startTime = event.startDate,
                        endTime = event.endDate,
                        hour = 7,
                        minute = 0,
                        title = event.name,
                        localDate = eventDate
                    )
                )
            }
        }
    }

    fun updateEvent(event: Events){
        viewModelScope.launch(Dispatchers.IO) {
            eventRepo.updateEvent(event)
            if (event.notification) {
                val eventDate = event.startDate.toLocalDate()

                // 1. Alarm for 12:00 AM
                scheduleNotification(
                    notificationAlarmManagerModel = NotificationAlarmManagerModel(
                        id = event.id,
                        type = 0,
                        startTime = event.startDate,
                        endTime = event.endDate,
                        hour = 0,
                        minute = 0,
                        title = event.name,
                        localDate = eventDate
                    )
                )

                // 2. Alarm for 7:00 AM
                scheduleNotification(
                    notificationAlarmManagerModel = NotificationAlarmManagerModel(
                        id = event.id,
                        type = 0,
                        startTime = event.startDate,
                        endTime = event.endDate,
                        hour = 7,
                        minute = 0,
                        title = event.name,
                        localDate = eventDate
                    )
                )
            }
        }
    }

    fun deleteEvent(event: Events){
        viewModelScope.launch(Dispatchers.IO) {
            eventRepo.deleteEvent(event)
        }
    }

    private fun hslToHex(h: Float, s: Float, l: Float): String {
        val c = (1f - Math.abs(2f * l - 1f)) * s
        val x = c * (1f - Math.abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        var r = 0f
        var g = 0f
        var b = 0f

        when {
            h < 60 -> { r = c; g = x; b = 0f }
            h < 120 -> { r = x; g = c; b = 0f }
            h < 180 -> { r = 0f; g = c; b = x }
            h < 240 -> { r = 0f; g = x; b = c }
            h < 300 -> { r = x; g = 0f; b = c }
            else -> { r = c; g = 0f; b = x }
        }

        val rInt = Math.round((r + m) * 255f).coerceIn(0, 255)
        val gInt = Math.round((g + m) * 255f).coerceIn(0, 255)
        val bInt = Math.round((b + m) * 255f).coerceIn(0, 255)

        return String.format("#%02X%02X%02X", rInt, gInt, bInt)
    }

    fun getUniqueColorForRange(startDate: Long, endDate: Long): String {
        val h = (0..359).random().toFloat()
        val s = 0.80f + (0..20).random() * 0.01f // Saturation: 0.80 to 1.00 (glowing, vivid)
        val l = 0.55f + (0..15).random() * 0.01f // Lightness: 0.55 to 0.70 (bright enough on pure black)
        return hslToHex(h, s, l)
    }

    fun scheduleNotification (notificationAlarmManagerModel: NotificationAlarmManagerModel){
        viewModelScope.launch {
            TimeFlowAlarmManagerService(context = context).scheduleSingleAlarm(notificationAlarmManagerModel = notificationAlarmManagerModel)
        }
    }


//    fun scheduleAllNotification() {
//        viewModelScope.launch {
//            val tasks = taskRepo.getTaskForScheduling(
//                start = LocalDate.now().atStartOfDay().atZone(
//                    ZoneId.systemDefault()
//                ).toInstant().toEpochMilli()
//            ).first()
//            val events = eventRepo.getEventsForNotification(
//                start = LocalDate.now().atStartOfDay().atZone(
//                    ZoneId.systemDefault()
//                ).toInstant().toEpochMilli()
//            ).first()
//
//            Log.d("TESTSCHEDULE", "tasks ---${tasks}")
//            Log.d("TESTSCHEDULE", "events ---${events}")
//
//            val modelEvent = events.map {
//                NotificationAlarmManagerModel(
//                    id = it.id,
//                    title = it.name,
//                    hour = it.eventNotificationTime.toHour(),
//                    minute = it.eventNotificationTime.toMinute(),
//                    localDate = it.createdAt.toLocalDate()
//                )
//            }
//            val modelTask = tasks.map {
//                NotificationAlarmManagerModel(
//                    id = it.id,
//                    title = it.name,
//                    hour = it.taskTime!!.toHour(),
//                    minute = it.taskTime.toMinute(),
//                    localDate = it.taskTime.toLocalDate()
//                )
//            }
//            Log.d("TESTSCHEDULE", "tasks : ${modelTask}")
//            Log.d("TESTSCHEDULE", "events  : ${modelEvent}")
////
//
//            val notificationModel =  modelTask + modelEvent
//
//            TimeFlowAlarmManagerService(context = context).scheduleNotification(
//                notificationAlarmManagerModel = notificationModel
//            )
//
//        }
//    }
    suspend fun saveOnBoarding() {
        dataStoreRepo.saveOnBoarding(completed = true)
    }

    fun readOnBoardingState(): Flow<Boolean> {
        return dataStoreRepo.readOnBoarding()
    }


    suspend fun saveSelectedCalenderType(type: Int) {
        dataStoreRepo.selectedCalendar(
            type = type
        )
    }

    fun readCalendarType(): Flow<Int> {
        return dataStoreRepo.readCalenderType()
    }


    fun saveName(name: String) {
        viewModelScope.launch {
            dataStoreRepo.saveName(name = name)
        }
    }

    fun readName(): Flow<String> {
        return dataStoreRepo.readName()
    }


}


