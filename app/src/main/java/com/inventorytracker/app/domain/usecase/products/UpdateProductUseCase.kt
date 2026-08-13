package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.repository.ProductRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    sealed interface Result {
        data object Success : Result
        data object NotFound : Result
        data object BarcodeAlreadyExists : Result
    }

    suspend operator fun invoke(
        id: Long,
        barcode: String,
        barcodeFormat: BarcodeFormat,
        name: String,
        description: String?,
        categoryId: Long?,
        lowStockThreshold: Int,
        unit: String,
        now: Long = System.currentTimeMillis(),
    ): Result {
        val existing = repository.getById(id) ?: return Result.NotFound
        val trimmedBarcode = barcode.trim()
        val trimmedName = name.trim()
        if (trimmedBarcode.isBlank() || trimmedName.isBlank()) {
            return Result.BarcodeAlreadyExists
        }
        if (trimmedBarcode != existing.barcode && repository.getByBarcode(trimmedBarcode) != null) {
            return Result.BarcodeAlreadyExists
        }

        repository.updateProduct(
            existing.copy(
                barcode = trimmedBarcode,
                barcodeFormat = barcodeFormat,
                name = trimmedName,
                description = description?.takeIf { it.isNotBlank() },
                categoryId = categoryId,
                lowStockThreshold = lowStockThreshold.coerceAtLeast(0),
                unit = unit.ifBlank { com.inventorytracker.app.domain.model.DEFAULT_UNIT },
                updatedAt = now,
            )
        )
        return Result.Success
    }
}
