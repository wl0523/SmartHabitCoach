package com.example.smarthabitcoach.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smarthabitcoach.domain.usecase.GenerateDailyNudgeUseCase
import com.example.smarthabitcoach.domain.usecase.GetHabitsUseCase
import com.example.smarthabitcoach.domain.usecase.GetStatisticsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DailyNudgeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getHabits: GetHabitsUseCase,
    private val getStatistics: GetStatisticsUseCase,
    private val generateDailyNudge: GenerateDailyNudgeUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val habits = getHabits().first()
            val stats  = getStatistics().first()
            val nudge  = generateDailyNudge(habits, stats)
            postNotification(nudge.message)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun postNotification(message: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Habit Nudges",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "AI-generated daily coaching nudges" }
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Your Daily Habit Nudge 🎯")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME               = "DailyNudgeWorker"
        private const val CHANNEL_ID      = "daily_nudge_channel"
        private const val NOTIFICATION_ID = 1002
    }
}

