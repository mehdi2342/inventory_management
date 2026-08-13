package com.inventorytracker.app.ui.scanner

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.usecase.products.LookupBarcodeUseCase
import com.inventorytracker.app.scanner.BarcodeFeedback
import com.inventorytracker.app.scanner.BarcodeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val lookupBarcode: LookupBarcodeUseCase,
    private val feedback: BarcodeFeedback,
) : ViewModel() {

    sealed interface NavigationEvent {
        data class Found(val productId: Long) : NavigationEvent
        data class NewBarcode(val barcode: String, val format: BarcodeFormat) : NavigationEvent
    }

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 4)
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

    private var lastScannedValue: String? = null
    private var lastScannedAt = 0L

    fun onBarcodeDetected(result: BarcodeResult) {
        val now = SystemClock.elapsedRealtime()
        if (result.rawValue == lastScannedValue && now - lastScannedAt < SCAN_COOLDOWN_MS) {
            return
        }
        lastScannedValue = result.rawValue
        lastScannedAt = now
        feedback.onBarcodeDetected()
        resolveBarcode(result.rawValue, result.format)
    }

    fun onManualEntry(barcode: String, format: BarcodeFormat) {
        val trimmed = barcode.trim()
        if (trimmed.isEmpty()) return
        feedback.onBarcodeDetected()
        resolveBarcode(trimmed, format)
    }

    private fun resolveBarcode(barcode: String, format: BarcodeFormat) {
        viewModelScope.launch {
            val product = lookupBarcode(barcode)
            _navigationEvents.emit(
                if (product != null) {
                    NavigationEvent.Found(product.id)
                } else {
                    NavigationEvent.NewBarcode(barcode, format)
                }
            )
        }
    }

    override fun onCleared() {
        feedback.release()
        super.onCleared()
    }

    companion object {
        private const val SCAN_COOLDOWN_MS = 1_500L
    }
}
