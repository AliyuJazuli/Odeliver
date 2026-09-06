package com.hydr.odeliver.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hydr.odeliver.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class WeeklyReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        if (dayOfWeek == Calendar.FRIDAY) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showNotification(
                "Weekly Summary",
                "Your weekly report is ready! Check how your business performed this week.",
                1002
            )
        }
        
        return Result.success()
    }
}
