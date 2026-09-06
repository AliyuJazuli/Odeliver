package com.hydr.odeliver.repository

import com.hydr.odeliver.SaleDao
import com.hydr.odeliver.SaleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao
) {
    fun getAllSalesByUser(uid: String): Flow<List<SaleEntity>> = saleDao.getAllSalesByUser(uid)

    suspend fun upsertSale(sale: SaleEntity) = saleDao.upsertSale(sale)

    suspend fun deleteSaleById(id: Int) = saleDao.deleteSaleById(id)

    suspend fun softDeleteSaleById(id: Int) = saleDao.softDeleteSaleById(id)

    suspend fun clearAllSales() = saleDao.clearAllSales()
}
