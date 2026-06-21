package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SittingDao {
    @Query("SELECT * FROM sitting_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SittingSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SittingSession)

    @Delete
    suspend fun deleteSession(session: SittingSession)

    @Query("DELETE FROM sitting_sessions")
    suspend fun clearAll()
}
