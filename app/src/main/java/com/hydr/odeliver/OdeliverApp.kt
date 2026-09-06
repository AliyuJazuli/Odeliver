package com.hydr.odeliver

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.hydr.odeliver.workers.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class OdeliverApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationScheduler.scheduleDailyNotifications(this)
        NotificationScheduler.scheduleWeeklyNotifications(this)
        NotificationScheduler.scheduleMonthlyNotifications(this)
    }
}
