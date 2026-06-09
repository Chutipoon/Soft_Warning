package com.softwarn.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSessionDao {
    @Insert suspend fun insert(session: AppSession)
    @Query("SELECT * FROM app_sessions WHERE packageName = :pkg ORDER BY startTime DESC LIMIT 50")
    fun getRecentSessions(pkg: String): Flow<List<AppSession>>
    @Query("SELECT SUM(durationMs) FROM app_sessions WHERE packageName = :pkg AND startTime > :since")
    suspend fun getTotalDurationSince(pkg: String, since: Long): Long?
    @Query("DELETE FROM app_sessions WHERE startTime < :before")
    suspend fun deleteOlderThan(before: Long)
}
