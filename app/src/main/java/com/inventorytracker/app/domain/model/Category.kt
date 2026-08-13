package com.inventorytracker.app.domain.model

const val DEFAULT_CATEGORY_COLOR = "#808080"

/**
 * Organizational grouping for products (e.g. Electronics, Groceries).
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val colorHex: String = DEFAULT_CATEGORY_COLOR,
    val createdAt: Long,
    val updatedAt: Long,
)
