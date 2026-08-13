package com.inventorytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val DEFAULT_COLOR_HEX = "#808080"

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val colorHex: String = DEFAULT_COLOR_HEX,
    val createdAt: Long,
    val updatedAt: Long,
)
