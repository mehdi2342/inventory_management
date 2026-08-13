package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    operator fun invoke(query: String): Flow<List<Product>> =
        repository.observeSearch(query.trim())
}
