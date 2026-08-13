package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.BarcodeFormat
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    sealed interface Result {
        data class Success(val productId: Long) : Result
        data object BarcodeAlreadyExists : Result
    }

    suspend operator fun invoke(
        barcode: String,
        barcodeFormat: BarcodeFormat,
        name: String,
        description: String? = null,
        quantity: Int = 0,
        categoryId: Long? = null,
        lowStockThreshold: Int = com.inventorytracker.app.domain.model.DEFAULT_LOW_STOCK_THRESHOLD,
        unit: String = com.inventorytracker.app.domain.model.DEFAULT_UNIT,
        now: Long = System.currentTimeMillis(),
    ): Result {
        val trimmedBarcode = barcode.trim()
        val trimmedName = name.trim()
        if (trimmedBarcode.isBlank() || trimmedName.isBlank()) {
            return Result.BarcodeAlreadyExists
        }
        if (repository.getByBarcode(trimmedBarcode) != null) {
            return Result.BarcodeAlreadyExists
        }

        val productId = repository.addProduct(
            Product(
                barcode = trimmedBarcode,
                barcodeFormat = barcodeFormat,
                name = trimmedName,
                description = description?.takeIf { it.isNotBlank() },
                quantity = quantity.coerceAtLeast(0),
                categoryId = categoryId,
                lowStockThreshold = lowStockThreshold.coerceAtLeast(0),
                unit = unit.ifBlank { com.inventorytracker.app.domain.model.DEFAULT_UNIT },
                createdAt = now,
                updatedAt = now,
            )
        )
        return Result.Success(productId)
    }
}
