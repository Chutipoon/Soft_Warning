package com.softwarn.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PackageUsage(
    val packageName: String,
    val totalMs: Long
)

data class DailyUsage(
    val day: String,
    val totalMs: Long
)

@Dao
interface AppSessionDao {
    @Insert suspend fun insert(session: AppSession)
    @Query("SELECT * FROM app_sessions WHERE packageName = :pkg ORDER BY startTime DESC LIMIT 50")
    fun getRecentSessions(pkg: String): Flow<List<AppSession>>
    @Query("SELECT SUM(durationMs) FROM app_sessions WHERE packageName = :pkg AND startTime > :since")
    suspend fun getTotalDurationSince(pkg: String, since: Long): Long?
    @Query("DELETE FROM app_sessions WHERE startTime < :before")
    suspend fun deleteOlderThan(before: Long)
    @Query("""
        SELECT packageName, SUM(durationMs) as totalMs FROM app_sessions
        WHERE startTime >= :since
        GROUP BY packageName
        ORDER BY totalMs DESC
    """)
    fun getUsageSince(since: Long): Flow<List<PackageUsage>>
    @Query("""
        SELECT date(startTime / 1000, 'unixepoch', 'localtime') as day, SUM(durationMs) as totalMs
        FROM app_sessions
        WHERE startTime >= :since
        GROUP BY day
        ORDER BY day ASC
    """)
    fun getDailyUsageSince(since: Long): Flow<List<DailyUsage>>
    @Query("SELECT * FROM app_sessions ORDER BY startTime ASC")
    fun getAllSessions(): Flow<List<AppSession>>
}
