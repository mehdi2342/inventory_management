package com.inventorytracker.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.repository.CategoryRepository
import com.inventorytracker.app.domain.repository.ProductRepository
import com.inventorytracker.app.domain.usecase.categories.AddCategoryUseCase
import com.inventorytracker.app.domain.usecase.categories.DeleteCategoryUseCase
import com.inventorytracker.app.domain.usecase.categories.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryWithProductCount(
    val category: Category,
    val productCount: Int,
)

data class CategoriesUiState(
    val categories: List<CategoryWithProductCount> = emptyList(),
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    productRepository: ProductRepository,
    private val addCategory: AddCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
) : ViewModel() {

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.observeAll(),
        productRepository.observeAll(),
    ) { categories, products ->
        CategoriesUiState(
            categories = categories.map { category ->
                CategoryWithProductCount(
                    category = category,
                    productCount = products.count { it.categoryId == category.id },
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    fun saveCategory(
        id: Long?,
        name: String,
        description: String,
        colorHex: String,
    ) {
        viewModelScope.launch {
            val result = if (id == null) {
                addCategory(name, description, colorHex)
            } else {
                updateCategory(id, name, description, colorHex)
            }
            when (result) {
                is AddCategoryUseCase.Result.Success -> _messages.emit("Category created")
                AddCategoryUseCase.Result.NameAlreadyExists -> _messages.emit("A category with this name already exists")
                UpdateCategoryUseCase.Result.Success -> _messages.emit("Category updated")
                UpdateCategoryUseCase.Result.NotFound -> _messages.emit("Category not found")
                UpdateCategoryUseCase.Result.NameAlreadyExists -> _messages.emit("A category with this name already exists")
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            when (val result = deleteCategory(categoryId)) {
                DeleteCategoryUseCase.Result.Success -> _messages.emit("Category deleted. Products moved to Uncategorized.")
                DeleteCategoryUseCase.Result.NotFound -> _messages.emit("Category not found")
                DeleteCategoryUseCase.Result.CannotDeleteDefault -> _messages.emit("Uncategorized cannot be deleted")
            }
        }
    }
}
