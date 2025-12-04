package com.ivy.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ivy.data.datastore.DatastoreKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TelegramDataRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val userId = dataStore.data.map { it[DatastoreKeys.TELEGRAM_USER_ID_KEY] }

    val botKey = dataStore.data.map { it[DatastoreKeys.TELEGRAM_BOT_ID_KEY] }


    suspend fun writeTelegramData(userId: String, botKey: String) = withContext(Dispatchers.IO) {
        dataStore.edit {
            it[DatastoreKeys.TELEGRAM_USER_ID_KEY] = userId
            it[DatastoreKeys.TELEGRAM_BOT_ID_KEY] = botKey
        }
    }

    suspend fun removeTelegramData() = withContext(Dispatchers.IO) {
        dataStore.edit {
            it.remove(DatastoreKeys.TELEGRAM_USER_ID_KEY)
            it.remove(DatastoreKeys.TELEGRAM_BOT_ID_KEY)
        }
    }
}