package com.inventorytracker.app.domain.usecase.categories

import com.inventorytracker.app.domain.repository.CategoryRepository
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    sealed interface Result {
        data object Success : Result
        data object NotFound : Result
        data object NameAlreadyExists : Result
    }

    suspend operator fun invoke(
        id: Long,
        name: String,
        description: String?,
        colorHex: String,
        now: Long = System.currentTimeMillis(),
    ): Result {
        val trimmedName = name.trim()
        val existing = repository.getById(id) ?: return Result.NotFound
        if (trimmedName.isBlank() || trimmedName != existing.name &&
            !repository.isNameAvailable(trimmedName, excludeId = id)
        ) {
            return Result.NameAlreadyExists
        }
        repository.updateCategory(
            existing.copy(
                name = trimmedName,
                description = description?.takeIf { it.isNotBlank() },
                colorHex = colorHex,
                updatedAt = now,
            )
        )
        return Result.Success
    }
}
