package com.softwarn.app.di

import android.content.Context
import com.softwarn.app.data.local.AppDatabase
import com.softwarn.app.data.local.AppSessionDao
import com.softwarn.app.data.local.WarningRuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideAppSessionDao(database: AppDatabase): AppSessionDao {
        return database.appSessionDao()
    }

    @Provides
    fun provideWarningRuleDao(database: AppDatabase): WarningRuleDao {
        return database.warningRuleDao()
    }
}
