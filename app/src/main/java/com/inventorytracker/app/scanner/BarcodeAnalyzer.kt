package com.inventorytracker.app.scanner

import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.inventorytracker.app.domain.model.BarcodeFormat
import kotlinx.coroutines.sync.Mutex

/**
 * CameraX [ImageAnalysis.Analyzer] that feeds frames to ML Kit's on-device
 * barcode scanner. Throttled to ~15 FPS with a mutex to guarantee only one
 * frame is being analyzed at a time.
 */
@OptIn(ExperimentalGetImage::class)
class BarcodeAnalyzer(
    private val onBarcodeDetected: (BarcodeResult) -> Unit,
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
            )
            .build()
    )
    private val mutex = Mutex()
    private var lastAnalysisTime = 0L

    @android.annotation.SuppressLint("UnsafeOptInUsageError")
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastAnalysisTime < FRAME_INTERVAL_MS) {
            imageProxy.close()
            return
        }
        if (!mutex.tryLock()) {
            imageProxy.close()
            return
        }
        lastAnalysisTime = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            mutex.unlock()
            return
        }

        try {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val barcode = barcodes.firstOrNull() ?: return@addOnSuccessListener
                    val rawValue = barcode.rawValue ?: return@addOnSuccessListener
                    onBarcodeDetected(
                        BarcodeResult(
                            rawValue = rawValue,
                            format = barcode.format.toDomainFormat(),
                        )
                    )
                }
                .addOnCompleteListener {
                    imageProxy.close()
                    mutex.unlock()
                }
        } catch (e: Exception) {
            imageProxy.close()
            mutex.unlock()
        }
    }

    fun close() {
        scanner.close()
    }

    companion object {
        private const val FRAME_INTERVAL_MS = 66L // ~15 frames per second

        private fun Int.toDomainFormat(): BarcodeFormat = when (this) {
            Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
            Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
            Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
            Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
            Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
            else -> BarcodeFormat.UNKNOWN
        }
    }
}
