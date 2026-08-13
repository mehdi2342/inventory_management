package com.inventorytracker.app.domain.repository

import com.inventorytracker.app.domain.model.InventoryLog
import com.inventorytracker.app.domain.model.InventoryLogWithProduct
import kotlinx.coroutines.flow.Flow

interface InventoryLogRepository {
    fun observeAll(): Flow<List<InventoryLogWithProduct>>
    fun observeByProduct(productId: Long): Flow<List<InventoryLog>>
    fun observeByProductAndTime(productId: Long, startTime: Long, endTime: Long): Flow<List<InventoryLog>>
    fun observeByTimeRange(startTime: Long, endTime: Long): Flow<List<InventoryLogWithProduct>>
    suspend fun addLog(log: InventoryLog): Long
}
