package com.inventorytracker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.inventorytracker.app.data.local.dao.CategoryDao
import com.inventorytracker.app.data.local.dao.InventoryLogDao
import com.inventorytracker.app.data.local.dao.ProductDao
import com.inventorytracker.app.data.local.entity.CategoryEntity
import com.inventorytracker.app.data.local.entity.InventoryLogEntity
import com.inventorytracker.app.data.local.entity.ProductEntity

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        InventoryLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun inventoryLogDao(): InventoryLogDao

    companion object {
        const val NAME = "inventory.db"

        /**
         * Default categories seeded on first launch. "Uncategorized" is the
         * reassignment target when a category is deleted.
         */
        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(name = "Uncategorized", colorHex = "#9E9E9E", createdAt = 0, updatedAt = 0),
            CategoryEntity(name = "Electronics", colorHex = "#3F51B5", createdAt = 0, updatedAt = 0),
            CategoryEntity(name = "Groceries", colorHex = "#4CAF50", createdAt = 0, updatedAt = 0),
            CategoryEntity(name = "Clothing", colorHex = "#FF9800", createdAt = 0, updatedAt = 0),
            CategoryEntity(name = "Office Supplies", colorHex = "#607D8B", createdAt = 0, updatedAt = 0),
            CategoryEntity(name = "Other", colorHex = "#795548", createdAt = 0, updatedAt = 0),
        )
    }
}
