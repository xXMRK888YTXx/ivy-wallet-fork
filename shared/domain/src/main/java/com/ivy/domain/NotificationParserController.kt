package com.ivy.domain

import kotlinx.coroutines.flow.StateFlow

interface NotificationParserController {
    val isConnected: StateFlow<Boolean>
    fun setConnected(connected: Boolean)
    fun forceRebind()
    fun checkAndRebindIfNeeded()
    fun syncState()
    fun updateParserEnabled(enabled: Boolean)
}
