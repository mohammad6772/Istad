package com.example.data

import kotlinx.coroutines.flow.Flow

class SittingRepository(private val sittingDao: SittingDao) {
    val allSessions: Flow<List<SittingSession>> = sittingDao.getAllSessions()

    suspend fun insertSession(session: SittingSession) {
        sittingDao.insertSession(session)
    }

    suspend fun deleteSession(session: SittingSession) {
        sittingDao.deleteSession(session)
    }

    suspend fun clearAll() {
        sittingDao.clearAll()
    }
}
