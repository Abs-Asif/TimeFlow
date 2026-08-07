package com.dev.timeflow.Data.Repo

import com.dev.timeflow.Data.Dao.EventDao
import com.dev.timeflow.Data.Model.Events
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventRepo @Inject constructor(
    private val eventDao: EventDao
) {
    suspend fun insertEvent(event: Events): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: Events) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: Events) {
        eventDao.deleteEvent(event)
    }

    fun getEventsForRange(start: Long, end: Long): Flow<List<Events>> {
        return eventDao.getEventsForRange(start, end)
    }

    fun getAllEvents(): Flow<List<Events>> {
        return eventDao.getAllEvents()
    }
}
