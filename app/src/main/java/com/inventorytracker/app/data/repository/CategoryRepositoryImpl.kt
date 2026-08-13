package com.inventorytracker.app.data.repository

import androidx.room.withTransaction
import com.inventorytracker.app.data.local.AppDatabase
import com.inventorytracker.app.data.local.dao.CategoryDao
import com.inventorytracker.app.data.local.dao.ProductDao
import com.inventorytracker.app.data.mapper.toDomain
import com.inventorytracker.app.data.mapper.toEntity
import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao,
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Category?> =
        categoryDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun getByName(name: String): Category? =
        categoryDao.getByName(name)?.toDomain()

    override suspend fun addCategory(category: Category): Long =
        categoryDao.insert(category.toEntity())

    override suspend fun updateCategory(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun deleteCategory(categoryId: Long) {
        database.withTransaction {
            val uncategorized = categoryDao.getByName(UNCATEGORIZED_NAME)?.id
            // Reassign products to "Uncategorized" so no data is lost.
            uncategorized?.let { productDao.reassignCategory(categoryId, it) }
            categoryDao.getById(categoryId)?.let { categoryDao.delete(it) }
        }
    }

    override suspend fun isNameAvailable(name: String, excludeId: Long?): Boolean {
        val existing = categoryDao.getByName(name) ?: return true
        return excludeId != null && existing.id == excludeId
    }

    companion object {
        const val UNCATEGORIZED_NAME = "Uncategorized"
    }
}
