package com.inventorytracker.app.domain.repository

import com.inventorytracker.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    fun observeById(id: Long): Flow<Category?>
    suspend fun getById(id: Long): Category?
    suspend fun getByName(name: String): Category?
    suspend fun addCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: Long)
    suspend fun isNameAvailable(name: String, excludeId: Long? = null): Boolean
}
