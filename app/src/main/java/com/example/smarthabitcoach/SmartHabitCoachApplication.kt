package com.example.smarthabitcoach

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.smarthabitcoach.worker.WeeklyInsightWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmartHabitCoachApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleWeeklyInsightWorker()
    }

    private fun scheduleWeeklyInsightWorker() {
        val request = OneTimeWorkRequestBuilder<WeeklyInsightWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            WeeklyInsightWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
