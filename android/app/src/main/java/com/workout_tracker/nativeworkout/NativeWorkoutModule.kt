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
import androidx.core.app.NotificationManagerCompat
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log


class NativeWorkoutModule(reactContext: ReactApplicationContext) :
    NativeWorkoutSpec(reactContext) {

    override fun getTypedExportedConstants(): Map<String, Any> {
        return mapOf(
            "maxRestDurationMs" to MAX_REST_DURATION_MS,
            "minRestDurationMs" to MIN_REST_DURATION_MS
        )
    }

    override fun vibrate() {
        val vibrator = getVibrator() ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATE_DURATION_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATE_DURATION_MS)
        }
    }

    override fun hasNotificationPermission(promise: Promise) {
        val manager = NotificationManagerCompat.from(reactApplicationContext)
        val appEnabled = manager.areNotificationsEnabled()
        val finishedChannel =
            manager.getNotificationChannelCompat(NotificationChannels.REST_FINISHED)
        val channelEnabled =
            finishedChannel?.importance != NotificationManagerCompat.IMPORTANCE_NONE
        promise.resolve(appEnabled && channelEnabled)
    }

    override fun openNotificationSettings() {
        val context = reactApplicationContext
        val packageName = context.packageName

        val notificationSettings =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            } else {
                appDetailsIntent(packageName)
            }

        if (!tryStartActivity(notificationSettings)) {
            tryStartActivity(appDetailsIntent(packageName))
        }
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

    private fun appDetailsIntent(packageName: String): Intent =
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )

    private fun tryStartActivity(intent: Intent): Boolean =
        try {
            reactApplicationContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: RuntimeException) {
            Log.w(NativeWorkoutSpec.NAME, "Cannot open settings: ${intent.action}", e)
            false
        }

    companion object {
        const val MAX_REST_DURATION_MS = 60 * 60 * 1000L
        const val MIN_REST_DURATION_MS = 1000L
        private const val VIBRATE_DURATION_MS = 200L
        private const val E_NOT_IMPLEMENTED = "E_NOT_IMPLEMENTED"
    }
}
