package com.inventorytracker.app.scanner

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Multi-sensory feedback on successful scan: brief 50ms vibration and a beep.
 */
class BarcodeFeedback @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, MAX_VOLUME)
    } catch (_: RuntimeException) {
        null
    }

    fun onBarcodeDetected() {
        vibrate()
        playBeep()
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun playBeep() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
    }

    fun release() {
        toneGenerator?.release()
    }

    private companion object {
        const val MAX_VOLUME = 80
    }
}
