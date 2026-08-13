package com.inventorytracker.app.domain.usecase.dashboard

import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.model.InventoryLogWithProduct
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.CategoryRepository
import com.inventorytracker.app.domain.repository.InventoryLogRepository
import com.inventorytracker.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class HomeData(
    val categories: List<CategoryWithCount>,
    val lowStockProducts: List<Product>,
    val recentActivity: List<InventoryLogWithProduct>,
)

data class CategoryWithCount(
    val category: Category,
    val productCount: Int,
)

/**
 * Aggregates everything the Home Dashboard needs in a single observable stream.
 */
class GetHomeDataUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val logRepository: InventoryLogRepository,
) {
    operator fun invoke(recentLimit: Int = 10): Flow<HomeData> =
        combine(
            categoryRepository.observeAll(),
            productRepository.observeAll(),
            productRepository.observeLowStock(),
            logRepository.observeAll(),
        ) { categories, products, lowStock, logs ->
            HomeData(
                categories = categories.map { category ->
                    CategoryWithCount(
                        category = category,
                        productCount = products.count { it.categoryId == category.id },
                    )
                },
                lowStockProducts = lowStock,
                recentActivity = logs.take(recentLimit),
            )
        }
}
