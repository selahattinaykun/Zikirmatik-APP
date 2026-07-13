package com.example.data

import kotlinx.coroutines.flow.Flow

class DhikrRepository(private val dhikrDao: DhikrDao) {
    val allSessions: Flow<List<DhikrSession>> = dhikrDao.getAllSessions()

    suspend fun insert(session: DhikrSession) = dhikrDao.insertSession(session)
}
