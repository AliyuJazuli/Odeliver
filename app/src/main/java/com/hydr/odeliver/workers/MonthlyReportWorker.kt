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
class MonthlyReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        if (dayOfWeek == Calendar.FRIDAY && isLastFridayOfMonth(calendar)) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showNotification(
                "Monthly Performance",
                "Your monthly report is here! Take a look at your achievements this month.",
                1003
            )
        }
        
        return Result.success()
    }

    private fun isLastFridayOfMonth(calendar: Calendar): Boolean {
        val currentMonth = calendar.get(Calendar.MONTH)
        val nextWeek = calendar.clone() as Calendar
        nextWeek.add(Calendar.DAY_OF_MONTH, 7)
        return nextWeek.get(Calendar.MONTH) != currentMonth
    }
}
