package com.inventorytracker.app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Seeds default categories when the database is first created.
 */
class PrepopulateCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val now = System.currentTimeMillis()
        AppDatabase.DEFAULT_CATEGORIES.forEach { category ->
            db.execSQL(
                "INSERT INTO categories (name, description, colorHex, createdAt, updatedAt) " +
                    "VALUES (?, NULL, ?, ?, ?)",
                arrayOf(
                    category.name,
                    category.colorHex,
                    now,
                    now,
                )
            )
        }
    }
}
