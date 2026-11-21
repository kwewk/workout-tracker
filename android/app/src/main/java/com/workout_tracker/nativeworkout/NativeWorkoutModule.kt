package com.workout_tracker.nativeworkout

import android.os.Vibrator
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.workout_tracker.NativeWorkoutSpec
import android.content.Context
import android.os.Build
import android.os.VibratorManager
import android.os.VibrationEffect

class NativeWorkoutModule(reactContext: ReactApplicationContext) :
    NativeWorkoutSpec(reactContext) {

  override fun getTypedExportedConstants(): Map<String, Any> {
   return mapOf("maxRestDurationMs" to MAX_REST_DURATION_MS, "minRestDurationMs" to MIN_REST_DURATION_MS)
  }

  override fun vibrate() {
    val vibrator = getVibrator() ?: return
    if (!vibrator.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(
        VibrationEffect.createOneShot(VIBRATE_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
      )
    } else {
      @Suppress("DEPRECATION")
      vibrator.vibrate(VIBRATE_DURATION_MS)
    }
  }

  override fun hasNotificationPermission(promise: Promise) {
    promise.reject(E_NOT_IMPLEMENTED, "hasNotificationPermission is not implemented yet")
  }

  override fun openNotificationSettings() {
  }

  override fun startRestTimer(options: ReadableMap, promise: Promise) {
    promise.reject(E_NOT_IMPLEMENTED, "startRestTimer is not implemented yet")
  }

  override fun cancelRestTimer() {
  }

  private fun getVibrator(): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val manager = reactApplicationContext
        .getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      manager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      reactApplicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

    companion object {
      const val MAX_REST_DURATION_MS = 60 * 60 * 1000L
      const val MIN_REST_DURATION_MS = 1000L
      private const val VIBRATE_DURATION_MS = 200L
      private const val E_NOT_IMPLEMENTED = "E_NOT_IMPLEMENTED"
    }
}
