package com.dev.timeflow.Data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dev.timeflow.Data.Dao.TaskDao
import com.dev.timeflow.Data.Dao.EventDao
import com.dev.timeflow.Data.Model.Tasks
import com.dev.timeflow.Data.Model.Events

@Database(
    entities = [Tasks::class, Events::class],
    version = 2
)
abstract class TaskDatabase : RoomDatabase(){
    abstract fun taskDao() : TaskDao
    abstract fun eventDao() : EventDao
}