package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.ChangeType
import com.inventorytracker.app.domain.model.MAX_QUANTITY
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Sets a product's quantity to an absolute value, recording a SET log entry.
 */
class SetInventoryUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    sealed interface Result {
        data class Success(val product: Product) : Result
        data object NegativeStock : Result
        data object QuantityOverflow : Result
        data object ProductNotFound : Result
    }

    suspend operator fun invoke(
        productId: Long,
        newQuantity: Int,
        reason: String? = null,
        now: Long = System.currentTimeMillis(),
    ): Result {
        val product = repository.getById(productId) ?: return Result.ProductNotFound
        if (newQuantity < 0) return Result.NegativeStock
        if (newQuantity > MAX_QUANTITY) return Result.QuantityOverflow

        repository.adjustQuantity(
            productId = productId,
            changeType = ChangeType.SET,
            amount = newQuantity - product.quantity,
            previousQuantity = product.quantity,
            newQuantity = newQuantity,
            reason = reason,
        )
        return Result.Success(product.copy(quantity = newQuantity, updatedAt = now))
    }
}
