package com.example.smarthabitcoach.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smarthabitcoach.domain.usecase.GenerateWeeklyInsightUseCase
import com.example.smarthabitcoach.domain.usecase.GetHabitsUseCase
import com.example.smarthabitcoach.domain.usecase.GetStatisticsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WeeklyInsightWorker — runs once per week via WorkManager.
 *
 * Pipeline:
 * 1. Fetch current habits + statistics from domain layer
 * 2. Call GenerateWeeklyInsightUseCase (checks Room cache first — idempotent)
 * 3. Post a notification with the AI-generated behavioral summary
 *
 * Cost: at most 1 GPT-4o-mini call/user/week ≈ $0.005/user/year
 */
@HiltWorker
class WeeklyInsightWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getHabits: GetHabitsUseCase,
    private val getStatistics: GetStatisticsUseCase,
    private val generateWeeklyInsight: GenerateWeeklyInsightUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val habits    = getHabits().first()
            val stats     = getStatistics().first()
            val insight   = generateWeeklyInsight(habits, stats)

            postNotification(
                title   = "Your Weekly Habit Report 📊",
                message = insight.recommendation
            )
            Result.success()
        } catch (_: Exception) {
            // Retry up to 3 times with exponential backoff (WorkManager default)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun postNotification(title: String, message: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        // Create channel (no-op on repeated calls, API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weekly Habit Insights",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "AI-generated weekly behavioral coaching reports" }
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME   = "WeeklyInsightWorker"
        private const val CHANNEL_ID      = "weekly_insight_channel"
        private const val NOTIFICATION_ID = 1001
    }
}

