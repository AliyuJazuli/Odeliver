package com.hydr.odeliver.workers

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleDailyNotifications(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DailyDeliveryWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(8, 0), TimeUnit.MILLISECONDS)
            .addTag("daily_delivery_notifications")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_delivery_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun scheduleWeeklyNotifications(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<WeeklyReportWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(9, 0, Calendar.FRIDAY), TimeUnit.MILLISECONDS)
            .addTag("weekly_report_notifications")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weekly_report_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun scheduleMonthlyNotifications(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<MonthlyReportWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(10, 0), TimeUnit.MILLISECONDS)
            .addTag("monthly_report_notifications")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "monthly_report_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateInitialDelay(hour: Int, minute: Int, dayOfWeek: Int? = null): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (dayOfWeek != null) {
            while (calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, if (dayOfWeek != null) 7 else 1)
        }
        
        return calendar.timeInMillis - now
    }
}
