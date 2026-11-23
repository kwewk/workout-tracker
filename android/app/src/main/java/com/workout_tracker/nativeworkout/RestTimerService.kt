package com.workout_tracker.nativeworkout

import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.workout_tracker.NativeWorkoutSpec
import com.workout_tracker.R

class RestTimerService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val finishRunnable = Runnable { onRestFinished() }

    private var wakeLock: PowerManager.WakeLock? = null
    private var endElapsedMs = 0L
    private var lastStartId = 0
    private var finishedTitle = ""
    private var finishedBody = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lastStartId = startId
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_CANCEL -> handleCancel(startId)
            else -> stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(finishRunnable)
        releaseWakeLock()
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

        finishedTitle = intent.getStringExtra(EXTRA_FINISHED_TITLE).orEmpty()
        finishedBody = intent.getStringExtra(EXTRA_FINISHED_BODY).orEmpty()
        NotificationManagerCompat.from(this).cancel(FINISHED_NOTIFICATION_ID)

        scheduleFinish(endsAtMs - System.currentTimeMillis())
    }

    private fun handleCancel(startId: Int) {
        handler.removeCallbacks(finishRunnable)
        NotificationManagerCompat.from(this).cancel(FINISHED_NOTIFICATION_ID)
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
    }

    private fun scheduleFinish(remainingMs: Long) {
        handler.removeCallbacks(finishRunnable)
        endElapsedMs = SystemClock.elapsedRealtime() + remainingMs

        if (remainingMs <= 0L) {
            onRestFinished()
            return
        }

        acquireWakeLock(remainingMs + WAKE_LOCK_EXTRA_MS)
        handler.postDelayed(finishRunnable, remainingMs)
    }

    private fun onRestFinished() {
        val leftMs = endElapsedMs - SystemClock.elapsedRealtime()
        if (leftMs > 0L) {
            acquireWakeLock(leftMs + WAKE_LOCK_EXTRA_MS)
            handler.postDelayed(finishRunnable, leftMs)
            return
        }

        Haptics.vibrateFinish(this)
        if (!isAppInForeground()) {
            showFinishedNotification()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(lastStartId)
        releaseWakeLock()
    }

    private fun isAppInForeground(): Boolean {
        val info = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(info)
        return info.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    private fun showFinishedNotification() {
        val manager = NotificationManagerCompat.from(this)
        if (!manager.areNotificationsEnabled()) return

        try {
            manager.notify(FINISHED_NOTIFICATION_ID, buildFinishedNotification())
        } catch (e: SecurityException) {
            Log.w(NativeWorkoutSpec.NAME, "Cannot show rest finished notification", e)
        }
    }

    private fun acquireWakeLock(timeoutMs: Long) {
        val lock = wakeLock ?: getSystemService(PowerManager::class.java)
            ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
            ?.apply { setReferenceCounted(false) }
            ?.also { wakeLock = it }

        lock?.acquire(timeoutMs)
    }

    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
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

    private fun buildFinishedNotification(): Notification =
        NotificationCompat.Builder(this, NotificationChannels.REST_FINISHED)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(finishedTitle)
            .setContentText(finishedBody)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppIntent())
            .build()

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
        const val EXTRA_FINISHED_TITLE = "finishedTitle"
        const val EXTRA_FINISHED_BODY = "finishedBody"

        const val NOTIFICATION_ID = 1
        const val FINISHED_NOTIFICATION_ID = 2

        private const val WAKE_LOCK_TAG = "workout_tracker:RestTimer"
        private const val WAKE_LOCK_EXTRA_MS = 10_000L
    }
}
