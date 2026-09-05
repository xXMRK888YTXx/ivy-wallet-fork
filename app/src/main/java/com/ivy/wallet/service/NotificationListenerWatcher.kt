package com.ivy.wallet.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

object NotificationListenerWatcher {

    fun isNotificationAccessGranted(context: Context): Boolean {
        val flat =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(context.packageName)
    }

    fun tryRequestRebind(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val componentName = ComponentName(context, IvyNotificationListenerService::class.java)
            return try {
                NotificationListenerService.requestRebind(componentName)
                Timber.d("NotificationListenerWatcher: requested rebind via API")
                true
            } catch (t: Throwable) {
                Timber.w(t, "NotificationListenerWatcher: failed to requestRebind")
                false
            }
        }
        return false
    }

    suspend fun toggleComponentRebind(context: Context, delayMs: Long = 300L) {
        val componentName = ComponentName(context, IvyNotificationListenerService::class.java)
        val pm = context.packageManager

        try {
            Timber.d("NotificationListenerWatcher: disabling component for rebind...")
            pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (t: Throwable) {
            Timber.e(t, "NotificationListenerWatcher: failed to disable component")
            return
        }

        // Essential delay for PackageManager & NotificationManagerService to process unbind
        delay(delayMs)

        try {
            Timber.d("NotificationListenerWatcher: re-enabling component...")
            pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            Timber.d("NotificationListenerWatcher: successfully re-enabled component")
        } catch (t: Throwable) {
            Timber.e(t, "NotificationListenerWatcher: failed to re-enable component")
            return
        }

        // On Android 7+, give NMS a short pause and nudge requestRebind
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            delay(150L.milliseconds)
            try {
                NotificationListenerService.requestRebind(componentName)
                Timber.d("NotificationListenerWatcher: post-toggle requestRebind sent")
            } catch (t: Throwable) {
                Timber.w(t, "NotificationListenerWatcher: post-toggle requestRebind failed")
            }
        }
    }

    suspend fun rebindService(context: Context, isConnectedProvider: () -> Boolean = { false }) {
        // 1. Try standard requestRebind first without touching component settings
        val requestSent = tryRequestRebind(context)
        if (requestSent) {
            // Give NMS a short window to connect
            delay(400L.milliseconds)
            if (isConnectedProvider()) {
                Timber.d("NotificationListenerWatcher: successfully reconnected via requestRebind")
                return
            }
            Timber.w("NotificationListenerWatcher: requestRebind did not restore connection, falling back to toggle trick")
        }

        // 2. Fallback: Component toggle trick with delay
        toggleComponentRebind(context)
    }

    fun forceRebindService(context: Context, isConnectedProvider: () -> Boolean = { false }) {
        CoroutineScope(Dispatchers.Default).launch {
            rebindService(context, isConnectedProvider)
        }
    }
}
