package com.inventorytracker.app.scanner

import com.inventorytracker.app.domain.model.BarcodeFormat

/**
 * Result of a successful barcode detection.
 */
data class BarcodeResult(
    val rawValue: String,
    val format: BarcodeFormat,
)
