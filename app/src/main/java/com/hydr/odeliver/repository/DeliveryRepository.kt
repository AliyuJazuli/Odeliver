package com.hydr.odeliver.repository

import com.hydr.odeliver.DeliveryDao
import com.hydr.odeliver.DeliveryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryRepository @Inject constructor(
    private val deliveryDao: DeliveryDao
) {
    val allDeliveries: Flow<List<DeliveryEntity>> = deliveryDao.getAllDeliveries()

    suspend fun upsertDelivery(delivery: DeliveryEntity) {
        deliveryDao.upsertDelivery(delivery)
        // TODO: Sync with Firebase here
    }

    suspend fun clearAllDeliveries() {
        deliveryDao.clearAllDeliveries()
        // TODO: Sync with Firebase here
    }

    // Example of a sync function
    suspend fun syncWithRemote() {
        // Logic to fetch from Firestore and update Room
    }
}
