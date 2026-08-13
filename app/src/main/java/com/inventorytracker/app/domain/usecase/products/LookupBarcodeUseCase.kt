package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Looks a product up by its barcode during scanning. Returns null when the
 * barcode is unknown (new product flow).
 */
class LookupBarcodeUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(barcode: String): Product? = repository.getByBarcode(barcode)
}
