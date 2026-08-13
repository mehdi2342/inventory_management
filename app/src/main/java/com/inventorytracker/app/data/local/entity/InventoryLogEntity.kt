package com.inventorytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inventorytracker.app.domain.model.ChangeType

@Entity(
    tableName = "inventory_logs",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["timestamp"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class InventoryLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val changeType: ChangeType,
    val amount: Int,
    val previousQuantity: Int,
    val newQuantity: Int,
    val reason: String? = null,
    val timestamp: Long,
)
