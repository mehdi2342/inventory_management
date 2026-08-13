package com.inventorytracker.app.ui.components

import android.content.Context
import com.inventorytracker.app.domain.model.Category
import com.inventorytracker.app.domain.model.ChangeType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Parses a hex color like "#3F51B5" to a Compose-friendly long value.
 */
fun parseHexColor(hex: String, fallback: Long = 0xFF808080): Long {
    val normalized = hex.removePrefix("#")
    return normalized.toLongOrNull(16)?.let { it or 0xFF000000L } ?: fallback
}

/**
 * A small selection of brand colors used for new categories.
 */
val BRAND_COLORS = listOf(
    "#3F51B5", "#4CAF50", "#FF9800", "#607D8B", "#E91E63", "#9C27B0",
    "#00BCD4", "#FFEB3B", "#795548", "#F44336", "#009688", "#9E9E9E",
)

fun formatTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return format.format(Date(timestamp))
}

fun changeTypeLabel(context: Context, type: ChangeType): String = when (type) {
    ChangeType.INCREASE -> context.getString(com.inventorytracker.app.R.string.change_increase)
    ChangeType.DECREASE -> context.getString(com.inventorytracker.app.R.string.change_decrease)
    ChangeType.SET -> context.getString(com.inventorytracker.app.R.string.change_set)
    ChangeType.UNDO -> context.getString(com.inventorytracker.app.R.string.change_undo)
}

fun categoryColor(category: Category?): Long = parseHexColor(category?.colorHex.orEmpty())
