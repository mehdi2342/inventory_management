package com.inventorytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.inventorytracker.app.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name COLLATE NOCASE ASC")
    fun observeByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query(
        "SELECT * FROM products WHERE name LIKE '%' || :query || '%' " +
            "OR barcode LIKE '%' || :query || '%' " +
            "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeSearch(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE quantity <= lowStockThreshold ORDER BY quantity ASC")
    fun observeLowStock(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("UPDATE products SET quantity = :newQuantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateQuantity(id: Long, newQuantity: Int, updatedAt: Long)

    @Query("UPDATE products SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)
}
