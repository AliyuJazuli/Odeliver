package com.hydr.odeliver.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hydr.odeliver.DeliveryDao
import com.hydr.odeliver.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class DailyDeliveryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val deliveryDao: DeliveryDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val sdfDate = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val today = sdfDate.format(Date())
        
        val deliveries = deliveryDao.getDeliveriesByDate(today)
        
        if (deliveries.isNotEmpty()) {
            val notificationHelper = NotificationHelper(applicationContext)
            
            val incomingCount = deliveries.count { !it.isOutgoing }
            val outgoingCount = deliveries.count { it.isOutgoing }
            
            val message = buildString {
                if (incomingCount > 0) {
                    append("$incomingCount incoming deliveries expected today. ")
                }
                if (outgoingCount > 0) {
                    append("$outgoingCount outgoing deliveries scheduled today.")
                }
            }
            
            if (message.isNotEmpty()) {
                notificationHelper.showNotification(
                    "Today's Deliveries",
                    message,
                    1001
                )
            }
        }
        
        return Result.success()
    }
}
