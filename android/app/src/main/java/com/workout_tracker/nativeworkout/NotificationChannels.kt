package com.workout_tracker.nativeworkout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.workout_tracker.R

object NotificationChannels {

    const val REST_TIMER = "rest_timer"
    const val REST_FINISHED = "rest_finished"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val restTimer = NotificationChannel(
            REST_TIMER,
            context.getString(R.string.rest_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.rest_channel_description)
            enableVibration(false)
            setShowBadge(false)
        }

        val restFinished = NotificationChannel(
            REST_FINISHED,
            context.getString(R.string.rest_finished_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.rest_finished_channel_description)
            enableVibration(false)
        }

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannels(listOf(restTimer, restFinished))
    }
}
