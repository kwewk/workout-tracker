package com.workout_tracker.nativeworkout

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.workout_tracker.R
import android.content.pm.ServiceInfo
import android.os.Build

class RestTimerService : Service() {

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_CANCEL -> handleCancel(startId)
            else -> stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    private fun handleStart(intent: Intent) {
        val endsAtMs = intent.getLongExtra(EXTRA_ENDS_AT_MS, 0L)
        val title = intent.getStringExtra(EXTRA_ONGOING_TITLE).orEmpty()
        val text = intent.getStringExtra(EXTRA_ONGOING_TEXT)

        val notification = buildOngoingNotification(endsAtMs, title, text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun handleCancel(startId: Int) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
    }

    private fun buildOngoingNotification(
        endsAtMs: Long,
        title: String,
        text: String?,
    ): Notification {
        val builder = NotificationCompat.Builder(this, NotificationChannels.REST_TIMER)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(title)
            .setWhen(endsAtMs)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(openAppIntent())

        if (!text.isNullOrBlank()) {
            builder.setContentText(text)
        }

        return builder.build()
    }

    private fun openAppIntent(): PendingIntent? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_START = "com.workout_tracker.nativeworkout.action.START_REST_TIMER"
        const val ACTION_CANCEL = "com.workout_tracker.nativeworkout.action.CANCEL_REST_TIMER"

        const val EXTRA_ENDS_AT_MS = "endsAtMs"
        const val EXTRA_ONGOING_TITLE = "ongoingTitle"
        const val EXTRA_ONGOING_TEXT = "ongoingText"

        const val NOTIFICATION_ID = 1

        @Volatile
        var isRunning = false
            private set
    }
}
