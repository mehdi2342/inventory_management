package com.inventorytracker.app.domain.usecase.categories

import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoryByIdUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    operator fun invoke(id: Long): Flow<Category?> = repository.observeById(id)
}
