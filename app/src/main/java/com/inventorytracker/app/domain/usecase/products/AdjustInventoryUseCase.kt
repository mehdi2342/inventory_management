package com.inventorytracker.app.domain.usecase.products

import com.inventorytracker.app.domain.model.ChangeType
import com.inventorytracker.app.domain.model.MAX_QUANTITY
import com.inventorytracker.app.domain.model.Product
import com.inventorytracker.app.domain.repository.ProductRepository
import javax.inject.Inject
import kotlin.math.abs

/**
 * Core inventory adjustment. Applies the validation rules from the PRD
 * (non-negative stock, non-zero amount, quantity overflow, missing product)
 * before delegating to the repository for the atomic DB transaction.
 */
class AdjustInventoryUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    sealed interface Result {
        data class Success(val product: Product) : Result
        data object ZeroAdjustment : Result
        data object NegativeStock : Result
        data object QuantityOverflow : Result
        data object ProductNotFound : Result
    }

    suspend operator fun invoke(
        productId: Long,
        delta: Int,
        reason: String? = null,
        now: Long = System.currentTimeMillis(),
    ): Result {
        if (delta == 0) return Result.ZeroAdjustment

        val product = repository.getById(productId) ?: return Result.ProductNotFound
        val newQuantity = product.quantity + delta
        if (newQuantity < 0) return Result.NegativeStock
        if (newQuantity > MAX_QUANTITY) return Result.QuantityOverflow

        val changeType = if (delta > 0) ChangeType.INCREASE else ChangeType.DECREASE
        repository.adjustQuantity(
            productId = productId,
            changeType = changeType,
            amount = abs(delta),
            previousQuantity = product.quantity,
            newQuantity = newQuantity,
            reason = reason,
        )
        return Result.Success(product.copy(quantity = newQuantity, updatedAt = now))
    }
}
