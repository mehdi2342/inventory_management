package com.inventorytracker.app.domain.usecase.categories

import com.inventorytracker.app.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Deletes a category and reassigns its products to the seeded "Uncategorized"
 * category so no product data is lost.
 */
class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    sealed interface Result {
        data object Success : Result
        data object NotFound : Result
        data object CannotDeleteDefault : Result
    }

    suspend operator fun invoke(categoryId: Long): Result {
        val category = repository.getById(categoryId) ?: return Result.NotFound
        if (category.name.equals(UNCATEGORIZED, ignoreCase = true)) {
            return Result.CannotDeleteDefault
        }
        repository.deleteCategory(categoryId)
        return Result.Success
    }

    companion object {
        const val UNCATEGORIZED = "Uncategorized"
    }
}
