package com.dev.timeflow.Data.Model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Events(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val name : String,
    val startDate : Long, // start date range millis (start of day)
    val endDate : Long, // end date range millis (end of day)
    val colorHex : String, // random colored line hex representation
    val notification : Boolean = true,
    val createdAt : Long
): Parcelable
