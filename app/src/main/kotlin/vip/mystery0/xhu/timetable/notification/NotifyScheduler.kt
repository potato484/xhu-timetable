package vip.mystery0.xhu.timetable.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotifyScheduler {
    private const val WORK_NAME = "NotifyWork"

    fun schedule(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<NotifyWorker>(
            repeatInterval = 12,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
