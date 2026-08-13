package com.inventorytracker.app.domain.model

const val DEFAULT_LOW_STOCK_THRESHOLD = 5
const val DEFAULT_UNIT = "pcs"
const val MAX_QUANTITY = 9_999_999

/**
 * An inventory item. [barcode] is the natural key used for scanning lookups.
 * [categoryId] is nullable; a null value means "Uncategorized".
 */
data class Product(
    val id: Long = 0,
    val barcode: String,
    val barcodeFormat: BarcodeFormat = BarcodeFormat.UNKNOWN,
    val name: String,
    val description: String? = null,
    val quantity: Int = 0,
    val categoryId: Long? = null,
    val lowStockThreshold: Int = DEFAULT_LOW_STOCK_THRESHOLD,
    val unit: String = DEFAULT_UNIT,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val isLowStock: Boolean
        get() = quantity <= lowStockThreshold
}
