package com.inventorytracker.app.ui.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.model.InventoryLog
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.CategoryRepository
import com.inventorytracker.app.domain.repository.InventoryLogRepository
import com.inventorytracker.app.domain.repository.ProductRepository
import com.inventorytracker.app.domain.usecase.products.AdjustInventoryUseCase
import com.inventorytracker.app.domain.usecase.products.DeleteProductUseCase
import com.inventorytracker.app.domain.usecase.products.SetInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val category: Category? = null,
    val logs: List<InventoryLog> = emptyList(),
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    inventoryLogRepository: InventoryLogRepository,
    private val adjustInventory: AdjustInventoryUseCase,
    private val setInventory: SetInventoryUseCase,
    private val deleteProduct: DeleteProductUseCase,
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle["productId"])

    private val productFlow = productRepository.observeById(productId)
    private val categoryFlow = combine(productFlow, categoryRepository.observeAll()) { product, categories ->
        product?.categoryId?.let { id -> categories.find { it.id == id } }
    }
    private val logsFlow = inventoryLogRepository.observeByProduct(productId)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages

    val uiState: StateFlow<ProductDetailUiState> = combine(
        productFlow,
        categoryFlow,
        logsFlow,
    ) { product, category, logs ->
        ProductDetailUiState(
            product = product,
            category = category,
            logs = logs,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductDetailUiState(),
    )

    fun adjust(delta: Int, reason: String?) {
        viewModelScope.launch {
            when (val result = adjustInventory(productId, delta, reason)) {
                is AdjustInventoryUseCase.Result.Success ->
                    _messages.emit("Quantity updated to ${result.product.quantity}")
                AdjustInventoryUseCase.Result.ZeroAdjustment ->
                    _messages.emit("Nothing to adjust")
                AdjustInventoryUseCase.Result.NegativeStock ->
                    _messages.emit("Cannot reduce below zero")
                AdjustInventoryUseCase.Result.QuantityOverflow ->
                    _messages.emit("Quantity too large (max 9,999,999)")
                AdjustInventoryUseCase.Result.ProductNotFound ->
                    _messages.emit("Product not found")
            }
        }
    }

    fun setQuantity(quantity: Int, reason: String?) {
        viewModelScope.launch {
            when (val result = setInventory(productId, quantity, reason)) {
                is SetInventoryUseCase.Result.Success ->
                    _messages.emit("Quantity set to ${result.product.quantity}")
                SetInventoryUseCase.Result.NegativeStock ->
                    _messages.emit("Cannot set below zero")
                SetInventoryUseCase.Result.QuantityOverflow ->
                    _messages.emit("Quantity too large (max 9,999,999)")
                SetInventoryUseCase.Result.ProductNotFound ->
                    _messages.emit("Product not found")
            }
        }
    }

    fun delete() {
        val product = uiState.value.product ?: return
        viewModelScope.launch {
            deleteProduct(product)
        }
    }
}
