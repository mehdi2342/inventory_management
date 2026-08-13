package com.inventorytracker.app.data.repository

import com.inventorytracker.app.data.local.dao.InventoryLogDao
import com.inventorytracker.app.data.local.dao.ProductDao
import com.inventorytracker.app.data.mapper.toDomain
import com.inventorytracker.app.data.mapper.toEntity
import com.inventorytracker.app.domain.model.InventoryLog
import com.inventorytracker.app.domain.model.InventoryLogWithProduct
import com.inventorytracker.app.domain.repository.InventoryLogRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class InventoryLogRepositoryImpl @Inject constructor(
    private val logDao: InventoryLogDao,
    private val productDao: ProductDao,
) : InventoryLogRepository {

    override fun observeAll(): Flow<List<InventoryLogWithProduct>> =
        combine(logDao.observeAll(), productDao.observeAll()) { logs, products ->
            val productsById = products.associateBy { it.id }
            logs.map { log ->
                InventoryLogWithProduct(
                    log = log.toDomain(),
                    product = productsById[log.productId]?.toDomain(),
                )
            }
        }

    override fun observeByProduct(productId: Long): Flow<List<InventoryLog>> =
        logDao.observeByProduct(productId).map { logs -> logs.map { it.toDomain() } }

    override fun observeByProductAndTime(
        productId: Long,
        startTime: Long,
        endTime: Long,
    ): Flow<List<InventoryLog>> =
        logDao.observeByProductAndTime(productId, startTime, endTime)
            .map { logs -> logs.map { it.toDomain() } }

    override fun observeByTimeRange(
        startTime: Long,
        endTime: Long,
    ): Flow<List<InventoryLogWithProduct>> =
        combine(
            logDao.observeByTimeRange(startTime, endTime),
            productDao.observeAll(),
        ) { logs, products ->
            val productsById = products.associateBy { it.id }
            logs.map { log ->
                InventoryLogWithProduct(
                    log = log.toDomain(),
                    product = productsById[log.productId]?.toDomain(),
                )
            }
        }

    override suspend fun addLog(log: InventoryLog): Long =
        logDao.insert(log.toEntity())
}
