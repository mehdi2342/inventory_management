package com.inventorytracker.app.domain.model

/**
 * Barcode symbologies supported by the scanning pipeline.
 * Mapped to ML Kit formats in the data layer.
 */
enum class BarcodeFormat(val displayName: String) {
    EAN_13("EAN-13"),
    UPC_A("UPC-A"),
    QR_CODE("QR Code"),
    CODE_128("Code 128"),
    CODE_39("Code 39"),
    UNKNOWN("Unknown"),
}
