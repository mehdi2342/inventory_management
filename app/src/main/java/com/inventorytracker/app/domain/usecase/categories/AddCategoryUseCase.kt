package com.inventorytracker.app.domain.usecase.categories

import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    sealed interface Result {
        data class Success(val categoryId: Long) : Result
        data object NameAlreadyExists : Result
    }

    suspend operator fun invoke(
        name: String,
        description: String? = null,
        colorHex: String,
        now: Long = System.currentTimeMillis(),
    ): Result {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return Result.NameAlreadyExists
        if (!repository.isNameAvailable(trimmedName)) return Result.NameAlreadyExists

        val id = repository.addCategory(
            Category(
                name = trimmedName,
                description = description?.takeIf { it.isNotBlank() },
                colorHex = colorHex,
                createdAt = now,
                updatedAt = now,
            )
        )
        return Result.Success(id)
    }
}
