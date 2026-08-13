package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsByCategoryUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    operator fun invoke(categoryId: Long): Flow<List<Product>> = repository.observeByCategory(categoryId)
}
