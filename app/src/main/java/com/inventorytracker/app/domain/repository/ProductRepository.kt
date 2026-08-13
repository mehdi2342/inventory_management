package com.inventorytracker.app.domain.repository

import com.inventorytracker.app.domain.model.ChangeType
import com.inventorytracker.app.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    fun observeById(id: Long): Flow<Product?>
    fun observeByCategory(categoryId: Long): Flow<List<Product>>
    fun observeSearch(query: String): Flow<List<Product>>
    fun observeLowStock(): Flow<List<Product>>

    suspend fun getById(id: Long): Product?
    suspend fun getByBarcode(barcode: String): Product?

    suspend fun addProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)

    /**
     * Atomically applies a quantity change and records the audit log entry.
     */
    suspend fun adjustQuantity(
        productId: Long,
        changeType: ChangeType,
        amount: Int,
        previousQuantity: Int,
        newQuantity: Int,
        reason: String?,
    )
}
