package com.workout_tracker.nativeworkout

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.workout_tracker.NativeWorkoutSpec

object Haptics {

    private const val TAP_DURATION_MS = 200L

    // Waveform timings in ms: wait, vibrate, pause, vibrate, pause, vibrate.
    private val FINISH_PATTERN = longArrayOf(0L, 400L, 200L, 400L, 200L, 400L)
    private const val NO_REPEAT = -1

    fun vibrateTap(context: Context) {
        val vibrator = getVibrator(context) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(TAP_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(TAP_DURATION_MS)
        }
    }

    fun vibrateFinish(context: Context) {
        val vibrator = getVibrator(context) ?: return

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(FINISH_PATTERN, NO_REPEAT),
                        VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM),
                    )
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(FINISH_PATTERN, NO_REPEAT),
                        alarmAudioAttributes(),
                    )
                }

                else -> {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(FINISH_PATTERN, NO_REPEAT, alarmAudioAttributes())
                }
            }
        } catch (e: RuntimeException) {
            Log.w(NativeWorkoutSpec.NAME, "Cannot vibrate at rest finish", e)
        }
    }

    private fun alarmAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

    private fun getVibrator(context: Context): Vibrator? {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        return vibrator?.takeIf { it.hasVibrator() }
    }
}
