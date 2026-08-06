package com.dev.timeflow.Managers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dev.timeflow.Data.Model.NotificationAlarmManagerModel
import com.dev.timeflow.Data.Repo.TaskRepo
import com.dev.timeflow.Managers.notification.TimeFlowAlarmManagerService
import com.dev.timeflow.View.utils.toHour
import com.dev.timeflow.View.utils.toLocalDate
import com.dev.timeflow.View.utils.toMinute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject


@AndroidEntryPoint
class BootCompleteReceiver (
) : BroadcastReceiver(){
    @Inject lateinit var taskRepo: TaskRepo
    override fun onReceive(context: Context, intent: Intent) {


        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            scope.launch {

                try {
                    val startDay = LocalDate.now()
                        .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()


                    val notificationAlarmManagerModel = taskRepo.getTaskForScheduling(
                        start = startDay
                    ).first().map {
                        NotificationAlarmManagerModel(
                            id = it.id,
                            title = it.name,
                            type = 1,
                            hour = it.taskTime!!.toHour(),
                            minute = it.taskTime.toMinute(),
                            localDate = it.taskTime.toLocalDate()
                        )
                    }

                    TimeFlowAlarmManagerService(context = context).scheduleNotification(
                        notificationAlarmManagerModel = notificationAlarmManagerModel
                    )
                } catch (e: Exception) {
                    println(e.localizedMessage)
                } finally {

                    pendingResult.finish()
                }


            }

        }
    }
}