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
    fun getDeliveriesByUser(uid: String): Flow<List<DeliveryEntity>> = deliveryDao.getDeliveriesByUser(uid)

    suspend fun upsertDelivery(delivery: DeliveryEntity) {
        deliveryDao.upsertDelivery(delivery)
    }

    suspend fun clearAllDeliveries() {
        deliveryDao.clearAllDeliveries()
    }

    suspend fun deleteDeliveryById(id: Int) {
        deliveryDao.deleteDeliveryById(id)
    }
}
