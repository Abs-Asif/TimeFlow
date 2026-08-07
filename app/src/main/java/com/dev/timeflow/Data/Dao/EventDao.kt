package com.dev.timeflow.Data.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.dev.timeflow.Data.Model.Events
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Events): Long

    @Upsert
    suspend fun updateEvent(event: Events)

    @Delete
    suspend fun deleteEvent(event: Events)

    // Select events overlapping with the range [start, end]
    @Query("SELECT * FROM Events WHERE startDate <= :end AND endDate >= :start ORDER BY createdAt DESC")
    fun getEventsForRange(start: Long, end: Long): Flow<List<Events>>

    @Query("SELECT * FROM Events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<Events>>
}
