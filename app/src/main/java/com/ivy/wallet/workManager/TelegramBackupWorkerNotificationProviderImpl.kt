package com.ivy.wallet.workManager

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.ivy.data.workManager.TelegramBackupWorkerNotificationProvider
import com.ivy.wallet.android.notification.IvyNotificationChannel
import com.ivy.wallet.android.notification.NotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TelegramBackupWorkerNotificationProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationService: NotificationService
) : TelegramBackupWorkerNotificationProvider {

    private val androidNotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private val notificationChannel by lazy { IvyNotificationChannel.TELEGRAM_BACKUP_ERRORS }
    override val foregroundNotification: Notification
        get() = notificationService.defaultIvyNotification(notificationChannel)
            .apply {
                setContentTitle("Telegram Backup")
                setContentText("Backing up Telegram...")
            }.build()

    override fun sendNotification(text: String) {
        val notification = notificationService.defaultIvyNotification(notificationChannel).apply {
            setContentTitle("Telegram Backup Error")
            setContentText(text)
        }

        notificationService.showNotification(notification, TELEGRAM_ERROR_NOTIFICATION_ID)
    }


    companion object {
        const val TELEGRAM_ERROR_NOTIFICATION_ID = 2345
    }

}