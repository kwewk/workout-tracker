package com.workout_tracker.nativeworkout

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.workout_tracker.NativeWorkoutSpec

class NativeWorkoutModule(reactContext: ReactApplicationContext) :
    NativeWorkoutSpec(reactContext) {

    override fun getTypedExportedConstants(): Map<String, Any> {
        return mapOf(
            "maxRestDurationMs" to MAX_REST_DURATION_MS,
            "minRestDurationMs" to MIN_REST_DURATION_MS
        )
    }

    override fun vibrate() {
        Haptics.vibrateTap(reactApplicationContext)
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
        val endsAtRaw = readEndsAtMs(options)
            ?: return promise.reject(E_INVALID_ARGS, "$KEY_ENDS_AT_MS must be a finite number")
        val ongoingTitle = readRequiredString(options, KEY_ONGOING_TITLE)
            ?: return promise.reject(E_INVALID_ARGS, "$KEY_ONGOING_TITLE must be a non-blank string")
        val finishedTitle = readRequiredString(options, KEY_FINISHED_TITLE)
            ?: return promise.reject(E_INVALID_ARGS, "$KEY_FINISHED_TITLE must be a non-blank string")
        val finishedBody = readRequiredString(options, KEY_FINISHED_BODY)
            ?: return promise.reject(E_INVALID_ARGS, "$KEY_FINISHED_BODY must be a non-blank string")
        if (!isOptionalString(options, KEY_ONGOING_TEXT)) {
            return promise.reject(E_INVALID_ARGS, "$KEY_ONGOING_TEXT must be a string if provided")
        }
        val ongoingText = options.getString(KEY_ONGOING_TEXT)?.takeIf { it.isNotBlank() }

        // Compare the raw double with now + limit: no overflow and no cut-off fraction.
        val nowMs = System.currentTimeMillis()
        if (endsAtRaw < nowMs + MIN_REST_DURATION_MS) {
            return promise.reject(
                E_ALREADY_FINISHED,
                "Less than $MIN_REST_DURATION_MS ms of rest is left"
            )
        }
        if (endsAtRaw > nowMs + MAX_REST_DURATION_MS) {
            return promise.reject(E_TOO_LONG, "Rest must not be longer than $MAX_REST_DURATION_MS ms")
        }
        val endsAtMs = endsAtRaw.toLong()

        val context = reactApplicationContext
        val intent = Intent(context, RestTimerService::class.java)
            .setAction(RestTimerService.ACTION_START)
            .putExtra(RestTimerService.EXTRA_ENDS_AT_MS, endsAtMs)
            .putExtra(RestTimerService.EXTRA_ONGOING_TITLE, ongoingTitle)
            .putExtra(RestTimerService.EXTRA_ONGOING_TEXT, ongoingText)
            .putExtra(RestTimerService.EXTRA_FINISHED_TITLE, finishedTitle)
            .putExtra(RestTimerService.EXTRA_FINISHED_BODY, finishedBody)

        try {
            val started = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            if (started == null) {
                promise.reject(E_START_NOT_ALLOWED, "The system refused to start the rest timer")
            } else {
                promise.resolve(null)
            }
        } catch (e: Exception) {
            if (isStartNotAllowed(e)) {
                promise.reject(
                    E_START_NOT_ALLOWED,
                    "Cannot start rest timer while the app is in the background",
                    e
                )
            } else {
                promise.reject(E_START_FAILED, e.message ?: "Cannot start rest timer", e)
            }
        }
    }

    override fun cancelRestTimer() {
        val context = reactApplicationContext
        NotificationManagerCompat.from(context).cancel(RestTimerService.FINISHED_NOTIFICATION_ID)

        val intent = Intent(context, RestTimerService::class.java)
            .setAction(RestTimerService.ACTION_CANCEL)

        try {
            context.startService(intent)
        } catch (e: RuntimeException) {
            Log.w(NativeWorkoutSpec.NAME, "Cannot cancel rest timer", e)
        }
    }

    /** Returns null when the value is missing, not a number, NaN or infinite. */
    private fun readEndsAtMs(options: ReadableMap): Double? {
        if (!options.hasKey(KEY_ENDS_AT_MS)) return null
        // getDouble throws on null or a non-number, so check the type first.
        if (options.getType(KEY_ENDS_AT_MS) != ReadableType.Number) return null
        val value = options.getDouble(KEY_ENDS_AT_MS)
        return value.takeIf { it.isFinite() }
    }

    /** Returns null when the value is missing, not a string or blank. */
    private fun readRequiredString(options: ReadableMap, key: String): String? {
        if (!options.hasKey(key) || options.getType(key) != ReadableType.String) return null
        return options.getString(key)?.takeIf { it.isNotBlank() }
    }

    /** Missing or null is fine; any other non-string type is not. */
    private fun isOptionalString(options: ReadableMap, key: String): Boolean {
        if (!options.hasKey(key)) return true
        val type = options.getType(key)
        return type == ReadableType.Null || type == ReadableType.String
    }

    private fun isStartNotAllowed(e: Exception): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            e is ForegroundServiceStartNotAllowedException

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

        private const val KEY_ENDS_AT_MS = "endsAtMs"
        private const val KEY_ONGOING_TITLE = "ongoingTitle"
        private const val KEY_ONGOING_TEXT = "ongoingText"
        private const val KEY_FINISHED_TITLE = "finishedTitle"
        private const val KEY_FINISHED_BODY = "finishedBody"

        private const val E_INVALID_ARGS = "E_INVALID_ARGS"
        private const val E_ALREADY_FINISHED = "E_ALREADY_FINISHED"
        private const val E_TOO_LONG = "E_TOO_LONG"
        private const val E_START_NOT_ALLOWED = "E_START_NOT_ALLOWED"
        private const val E_START_FAILED = "E_START_FAILED"
    }
}
