package com.ivy.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ivy.data.datastore.DatastoreKeys.IS_TELEGRAM_BACKUP_ENABLED_KEY
import com.ivy.data.datastore.DatastoreKeys.TELEGRAM_BACKUP_REPEAT_TIME_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TelegramBackupSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val isEnabled: Flow<Boolean> = dataStore.data.map { it[IS_TELEGRAM_BACKUP_ENABLED_KEY] ?: false }

    val telegramBackupRepeatTimeInMills = dataStore.data.map { it[TELEGRAM_BACKUP_REPEAT_TIME_KEY] ?: DEFAULT_TELEGRAM_BACKUP_REPEAT_TIME_IN_MILLS  }

    suspend fun writeEnabledState(newState: Boolean) = withContext(Dispatchers.IO) {
        dataStore.edit { it[IS_TELEGRAM_BACKUP_ENABLED_KEY] = newState }
    }

    suspend fun writeTelegramBackupRepeatTime(newTimeInMills: Long) = withContext(Dispatchers.IO) {
        dataStore.edit { it[TELEGRAM_BACKUP_REPEAT_TIME_KEY] = newTimeInMills }
    }

    companion object {
        const val DEFAULT_TELEGRAM_BACKUP_REPEAT_TIME_IN_MILLS = 21_600_000L
    }


}