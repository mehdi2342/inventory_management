package com.inventorytracker.app.ui.addeditproduct

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.repository.CategoryRepository
import com.inventorytracker.app.domain.repository.ProductRepository
import com.inventorytracker.app.domain.usecase.products.AddProductUseCase
import com.inventorytracker.app.domain.usecase.products.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditProductUiState(
    val barcode: String = "",
    val barcodeFormat: BarcodeFormat = BarcodeFormat.UNKNOWN,
    val name: String = "",
    val description: String = "",
    val quantity: String = "",
    val categoryId: Long? = null,
    val lowStockThreshold: String = "",
    val unit: String = "",
    val categories: List<Category> = emptyList(),
    val isEditing: Boolean = false,
    val productId: Long = -1,
    val isSaving: Boolean = false,
)

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val addProduct: AddProductUseCase,
    private val updateProduct: UpdateProductUseCase,
) : ViewModel() {

    private val isEditing = savedStateHandle.contains("productId")
    private val productId: Long = savedStateHandle.get<Long>("productId") ?: -1
    private val initialBarcode: String = savedStateHandle.get<String>("barcode") ?: ""
    private val initialFormat: BarcodeFormat =
        savedStateHandle.get<String>("format")
            ?.let { name -> BarcodeFormat.entries.firstOrNull { it.name == name } }
            ?: BarcodeFormat.UNKNOWN

    private val _isSaving = MutableStateFlow(false)
    private val _saved = MutableSharedFlow<Long>(extraBufferCapacity = 4)
    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 4)

    val saved: SharedFlow<Long> = _saved
    val errors: SharedFlow<String> = _errors

    private val fields = MutableStateFlow(
        AddEditProductUiState(
            barcode = initialBarcode,
            barcodeFormat = initialFormat,
            lowStockThreshold = "5",
            unit = "pcs",
        )
    )

    val uiState: StateFlow<AddEditProductUiState> = combine(
        fields,
        categoryRepository.observeAll(),
        _isSaving,
    ) { state, categories, saving ->
        state.copy(categories = categories, isEditing = isEditing, isSaving = saving)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AddEditProductUiState(),
    )

    init {
        if (isEditing) {
            viewModelScope.launch {
                val product = productRepository.getById(productId) ?: return@launch
                fields.value = fields.value.copy(
                    barcode = product.barcode,
                    barcodeFormat = product.barcodeFormat,
                    name = product.name,
                    description = product.description.orEmpty(),
                    quantity = if (product.quantity == 0) "" else product.quantity.toString(),
                    categoryId = product.categoryId,
                    lowStockThreshold = product.lowStockThreshold.toString(),
                    unit = product.unit,
                    productId = product.id,
                )
            }
        }
    }

    fun onBarcodeChange(value: String) = updateState { it.copy(barcode = value) }
    fun onFormatChange(value: BarcodeFormat) = updateState { it.copy(barcodeFormat = value) }
    fun onNameChange(value: String) = updateState { it.copy(name = value) }
    fun onDescriptionChange(value: String) = updateState { it.copy(description = value) }
    fun onQuantityChange(value: String) =
        updateState { it.copy(quantity = value.filter { c -> c.isDigit() }) }
    fun onCategoryChange(value: Long?) = updateState { it.copy(categoryId = value) }
    fun onThresholdChange(value: String) =
        updateState { it.copy(lowStockThreshold = value.filter { c -> c.isDigit() }) }
    fun onUnitChange(value: String) = updateState { it.copy(unit = value) }

    private fun updateState(transform: (AddEditProductUiState) -> AddEditProductUiState) {
        fields.value = transform(fields.value)
    }

    fun save() {
        val state = fields.value
        if (state.name.isBlank()) {
            viewModelScope.launch { _errors.emit("Please enter a product name") }
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            val quantity = state.quantity.toIntOrNull() ?: 0
            val threshold = state.lowStockThreshold.toIntOrNull() ?: 5
            if (state.isEditing) {
                val result = updateProduct(
                    id = state.productId,
                    barcode = state.barcode,
                    barcodeFormat = state.barcodeFormat,
                    name = state.name,
                    description = state.description,
                    categoryId = state.categoryId,
                    lowStockThreshold = threshold,
                    unit = state.unit,
                )
                when (result) {
                    UpdateProductUseCase.Result.Success -> _saved.emit(state.productId)
                    UpdateProductUseCase.Result.BarcodeAlreadyExists ->
                        _errors.emit("A product with this barcode already exists")
                    UpdateProductUseCase.Result.NotFound ->
                        _errors.emit("Product not found")
                }
            } else {
                val result = addProduct(
                    barcode = state.barcode,
                    barcodeFormat = state.barcodeFormat,
                    name = state.name,
                    description = state.description,
                    quantity = quantity,
                    categoryId = state.categoryId,
                    lowStockThreshold = threshold,
                    unit = state.unit,
                )
                when (result) {
                    is AddProductUseCase.Result.Success -> _saved.emit(result.productId)
                    AddProductUseCase.Result.BarcodeAlreadyExists ->
                        _errors.emit("A product with this barcode already exists")
                }
            }
            _isSaving.value = false
        }
    }
}
