package com.ivy.wallet.service

import android.content.Context
import com.ivy.base.legacy.SharedPrefs
import com.ivy.domain.NotificationParserController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationParserControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPrefs: SharedPrefs
) : NotificationParserController {

    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val isRebinding = AtomicBoolean(false)

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    override fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    override fun forceRebind() {
        Timber.d("NotificationParserController: forceRebind requested")
        controllerScope.launch {
            rebindInternal()
        }
    }

    override fun checkAndRebindIfNeeded() {
        val isParserEnabled = sharedPrefs.getBoolean(SharedPrefs.NOTIFICATION_PARSER_ENABLED, false)
        val isAccessGranted = NotificationListenerWatcher.isNotificationAccessGranted(context)
        val isConnected = _isConnected.value

        Timber.d("NotificationParserController: check -> isParserEnabled=$isParserEnabled, isAccessGranted=$isAccessGranted, isConnected=$isConnected")

        if (isParserEnabled && isAccessGranted && !isConnected) {
            Timber.w("NotificationParserController: service is disconnected while enabled, rebinding now...")
            controllerScope.launch {
                rebindInternal()
            }
        }
    }

    private suspend fun rebindInternal() {
        if (!isRebinding.compareAndSet(false, true)) {
            Timber.d("NotificationParserController: rebind already in progress, skipping")
            return
        }
        try {
            NotificationListenerWatcher.rebindService(context) { _isConnected.value }
        } finally {
            isRebinding.set(false)
        }
    }

    override fun syncState() {
        val isParserEnabled = sharedPrefs.getBoolean(SharedPrefs.NOTIFICATION_PARSER_ENABLED, false)
        Timber.d("NotificationParserController: syncState (isParserEnabled=$isParserEnabled)")

        if (isParserEnabled) {
            NotificationWatchdogWorker.schedule(context)
            checkAndRebindIfNeeded()
        } else {
            NotificationWatchdogWorker.cancel(context)
        }
    }

    override fun updateParserEnabled(enabled: Boolean) {
        sharedPrefs.putBoolean(SharedPrefs.NOTIFICATION_PARSER_ENABLED, enabled)
        syncState()
    }
}
