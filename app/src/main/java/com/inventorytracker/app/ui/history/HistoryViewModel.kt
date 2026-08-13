package com.inventorytracker.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventorytracker.app.domain.model.InventoryLogWithProduct
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.InventoryLogRepository
import com.inventorytracker.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class TimeRange { ALL, TODAY, WEEK, MONTH }

data class HistoryUiState(
    val entries: List<InventoryLogWithProduct> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedProductId: Long? = null,
    val timeRange: TimeRange = TimeRange.ALL,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    inventoryLogRepository: InventoryLogRepository,
    productRepository: ProductRepository,
) : ViewModel() {

    private val selectedProductId = MutableStateFlow<Long?>(null)
    private val timeRange = MutableStateFlow(TimeRange.ALL)

    val uiState: StateFlow<HistoryUiState> = combine(
        inventoryLogRepository.observeAll(),
        productRepository.observeAll(),
        selectedProductId,
        timeRange,
    ) { logs, products, productId, range ->
        val startTime = startTimeFor(range)
        val filtered = logs.filter { entry ->
            val inTime = startTime == null || entry.log.timestamp >= startTime
            val inProduct = productId == null || entry.log.productId == productId
            inTime && inProduct
        }
        HistoryUiState(
            entries = filtered,
            products = products,
            selectedProductId = productId,
            timeRange = range,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onProductFilterChange(productId: Long?) {
        selectedProductId.value = productId
    }

    fun onTimeRangeChange(range: TimeRange) {
        timeRange.value = range
    }

    private fun startTimeFor(range: TimeRange): Long? {
        val calendar = java.util.Calendar.getInstance()
        return when (range) {
            TimeRange.ALL -> null
            TimeRange.TODAY -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            TimeRange.WEEK -> calendar.timeInMillis - 7 * 24 * 60 * 60 * 1000L
            TimeRange.MONTH -> calendar.timeInMillis - 30 * 24 * 60 * 60 * 1000L
        }
    }
}
