package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sitting_sessions")
data class SittingSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val isSitting: Boolean, // true if sitting, false if standing
    val dateString: String // yyyy-MM-dd
)
