package com.drew654.mocklocations.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.drew654.mocklocations.R
import com.drew654.mocklocations.domain.model.MockControlState
import com.drew654.mocklocations.domain.model.isPauseVisible
import com.drew654.mocklocations.domain.model.isResumeVisible

class MockNotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "mock_location_channel"
        const val NOTIFICATION_ID = 1
    }

    private val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Mock Location Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Running location simulation in the background"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createServicePendingIntent(action: String): PendingIntent {
        return PendingIntent.getService(
            context,
            action.hashCode(),
            Intent(context, MockLocationService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun buildNotification(mockControlState: MockControlState): Notification {
        val stopMockingIntent = createServicePendingIntent(MockLocationService.ACTION_STOP_MOCKING_NOTIFICATION)
        val pauseMockingIntent = createServicePendingIntent(MockLocationService.ACTION_PAUSE_MOCKING_NOTIFICATION)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Location Mocking Active")
            .setContentText("Your location is currently being mocked")
            .setSmallIcon(R.drawable.baseline_my_location_24)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(R.drawable.baseline_stop_24, "Stop", stopMockingIntent)
            .apply {
                if (mockControlState.isPauseVisible()) {
                    addAction(
                        R.drawable.baseline_pause_24,
                        "Pause",
                        pauseMockingIntent
                    )
                } else if (mockControlState.isResumeVisible()) {
                    addAction(
                        R.drawable.baseline_play_arrow_24,
                        "Resume",
                        pauseMockingIntent
                    )
                }
            }
            .build()
    }
}
