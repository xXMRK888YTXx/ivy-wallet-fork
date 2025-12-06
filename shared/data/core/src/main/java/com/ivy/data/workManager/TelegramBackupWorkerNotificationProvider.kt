package com.ivy.data.workManager

import android.app.Notification
import android.app.NotificationChannel

interface TelegramBackupWorkerNotificationProvider {
    val foregroundNotification: Notification

    fun sendNotification(text: String)
}