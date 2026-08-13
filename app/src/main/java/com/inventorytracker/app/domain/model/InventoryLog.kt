package com.inventorytracker.app.domain.model

/**
 * Audit trail entry recording a single inventory modification.
 */
data class InventoryLog(
    val id: Long = 0,
    val productId: Long,
    val changeType: ChangeType,
    val amount: Int,
    val previousQuantity: Int,
    val newQuantity: Int,
    val reason: String? = null,
    val timestamp: Long,
)

/**
 * [InventoryLog] enriched with product info for list display.
 */
data class InventoryLogWithProduct(
    val log: InventoryLog,
    val product: Product?,
)
