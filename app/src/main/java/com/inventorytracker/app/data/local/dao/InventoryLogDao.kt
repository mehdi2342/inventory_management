package com.inventorytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.inventorytracker.app.data.local.entity.InventoryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryLogDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: InventoryLogEntity): Long

    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<InventoryLogEntity>>

    @Query("SELECT * FROM inventory_logs WHERE productId = :productId ORDER BY timestamp DESC")
    fun observeByProduct(productId: Long): Flow<List<InventoryLogEntity>>

    @Query(
        "SELECT * FROM inventory_logs WHERE productId = :productId " +
            "AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC"
    )
    fun observeByProductAndTime(productId: Long, startTime: Long, endTime: Long): Flow<List<InventoryLogEntity>>

    @Query(
        "SELECT * FROM inventory_logs WHERE timestamp BETWEEN :startTime AND :endTime " +
            "ORDER BY timestamp DESC"
    )
    fun observeByTimeRange(startTime: Long, endTime: Long): Flow<List<InventoryLogEntity>>
}
