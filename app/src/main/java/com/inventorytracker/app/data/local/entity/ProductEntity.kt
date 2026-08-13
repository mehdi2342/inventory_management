package com.inventorytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inventorytracker.app.domain.model.BarcodeFormat

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = true),
        Index(value = ["categoryId"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val barcodeFormat: BarcodeFormat,
    val name: String,
    val description: String? = null,
    val quantity: Int = 0,
    val categoryId: Long? = null,
    val lowStockThreshold: Int = 5,
    val unit: String = "pcs",
    val createdAt: Long,
    val updatedAt: Long,
)
