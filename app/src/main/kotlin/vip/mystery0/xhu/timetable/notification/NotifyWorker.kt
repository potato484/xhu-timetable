package vip.mystery0.xhu.timetable.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotifyWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            val action = NotifyAction(applicationContext)
            runCatching { action.checkNotifyCourse() }
            runCatching { action.checkNotifyExam() }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
