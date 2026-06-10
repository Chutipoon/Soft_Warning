package com.softwarn.app.service

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.softwarn.app.data.AppDatabase
import com.softwarn.app.data.AppSessionDao
import com.softwarn.app.data.WarningRule
import com.softwarn.app.data.WarningRuleDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UsageMonitorServiceTest {

    private lateinit var db: AppDatabase
    private lateinit var appSessionDao: AppSessionDao
    private lateinit var warningRuleDao: WarningRuleDao
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        appSessionDao = db.appSessionDao()
        warningRuleDao = db.warningRuleDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveSession() = runBlocking {
        val packageName = "com.test.app"
        val startTime = System.currentTimeMillis() - 10000
        val endTime = System.currentTimeMillis()

        val session = com.softwarn.app.data.AppSession(
            packageName = packageName,
            startTime = startTime,
            endTime = endTime
        )
        appSessionDao.insert(session)

        val recentSessions = appSessionDao.getRecentSessions(packageName).first()
        assertEquals(1, recentSessions.size)
        assertEquals(packageName, recentSessions[0].packageName)
        assertEquals(10000L, recentSessions[0].durationMs)
    }

    @Test
    fun testWarningRuleLogic() = runBlocking {
        val packageName = "com.test.app"
        val rule = WarningRule(
            packageName = packageName,
            appName = "Test App",
            intervalMinutes = 1,
            isEnabled = true
        )
        warningRuleDao.upsert(rule)

        val enabledRule = warningRuleDao.getEnabledRule(packageName)
        assertTrue(enabledRule != null)
        assertEquals(1, enabledRule?.intervalMinutes)
    }
}
