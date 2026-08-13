package com.inventorytracker.app.data.repository

import androidx.room.withTransaction
import com.inventorytracker.app.data.local.AppDatabase
import com.inventorytracker.app.data.local.dao.InventoryLogDao
import com.inventorytracker.app.data.local.dao.ProductDao
import com.inventorytracker.app.data.local.entity.InventoryLogEntity
import com.inventorytracker.app.data.mapper.toDomain
import com.inventorytracker.app.data.mapper.toEntity
import com.inventorytracker.app.domain.model.ChangeType
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val productDao: ProductDao,
    private val logDao: InventoryLogDao,
) : ProductRepository {

    override fun observeAll(): Flow<List<Product>> =
        productDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Product?> =
        productDao.observeById(id).map { it?.toDomain() }

    override fun observeByCategory(categoryId: Long): Flow<List<Product>> =
        productDao.observeByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun observeSearch(query: String): Flow<List<Product>> =
        productDao.observeSearch(query).map { list -> list.map { it.toDomain() } }

    override fun observeLowStock(): Flow<List<Product>> =
        productDao.observeLowStock().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Product? =
        productDao.getById(id)?.toDomain()

    override suspend fun getByBarcode(barcode: String): Product? =
        productDao.getByBarcode(barcode)?.toDomain()

    override suspend fun addProduct(product: Product): Long =
        productDao.insert(product.toEntity())

    override suspend fun updateProduct(product: Product) {
        productDao.update(product.toEntity())
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.delete(product.toEntity())
    }

    override suspend fun adjustQuantity(
        productId: Long,
        changeType: ChangeType,
        amount: Int,
        previousQuantity: Int,
        newQuantity: Int,
        reason: String?,
    ) {
        val timestamp = System.currentTimeMillis()
        database.withTransaction {
            productDao.updateQuantity(productId, newQuantity, timestamp)
            logDao.insert(
                InventoryLogEntity(
                    productId = productId,
                    changeType = changeType,
                    amount = amount,
                    previousQuantity = previousQuantity,
                    newQuantity = newQuantity,
                    reason = reason,
                    timestamp = timestamp,
                )
            )
        }
    }
}
