package com.inventorytracker.app.ui.productlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.CategoryRepository
import com.inventorytracker.app.domain.repository.ProductRepository
import com.inventorytracker.app.ui.components.categoryColor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class SortOrder { NAME, QUANTITY, RECENT }

data class ProductListItem(
    val product: Product,
    val categoryColor: Long,
    val categoryName: String?,
)

data class ProductListUiState(
    val categories: List<Category> = emptyList(),
    val items: List<ProductListItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val sortOrder: SortOrder = SortOrder.NAME,
) {
    val totalCount: Int get() = items.size
}

@HiltViewModel
class ProductListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val initialCategoryId = savedStateHandle.get<Long>("categoryId")?.takeIf { it >= 0 }

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow(initialCategoryId)
    private val sortOrder = MutableStateFlow(SortOrder.NAME)

    val uiState: StateFlow<ProductListUiState> = combine(
        productRepository.observeAll(),
        categoryRepository.observeAll(),
        searchQuery,
        selectedCategory,
        sortOrder,
    ) { products, categories, query, categoryId, order ->
        val filtered = products.filter { product ->
            val matchesQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.barcode.contains(query, ignoreCase = true)
            val matchesCategory = categoryId == null || product.categoryId == categoryId
            matchesQuery && matchesCategory
        }
        val categoryById = categories.associateBy { it.id }
        val sorted = when (order) {
            SortOrder.NAME -> filtered.sortedBy { it.name.lowercase() }
            SortOrder.QUANTITY -> filtered.sortedBy { it.quantity }
            SortOrder.RECENT -> filtered.sortedByDescending { it.createdAt }
        }
        ProductListUiState(
            categories = categories,
            items = sorted.map { product ->
                val category = categoryById[product.categoryId]
                ProductListItem(
                    product = product,
                    categoryColor = categoryColor(category),
                    categoryName = category?.name,
                )
            },
            searchQuery = query,
            selectedCategoryId = categoryId,
            sortOrder = order,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductListUiState(),
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onCategoryFilterChange(categoryId: Long?) {
        selectedCategory.value = categoryId
    }

    fun onSortOrderChange(order: SortOrder) {
        sortOrder.value = order
    }
}
