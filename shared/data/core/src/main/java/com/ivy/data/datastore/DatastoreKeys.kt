package com.ivy.data.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DatastoreKeys {
    @Deprecated("will be removed")
    val GITHUB_OWNER = stringPreferencesKey("github_backup_owner")

    @Deprecated("will be removed")
    val GITHUB_REPO = stringPreferencesKey("github_backup_repo")

    @Deprecated("will be removed")
    val GITHUB_PAT = stringPreferencesKey("github_backup_pat")

    @Deprecated("will be removed")
    val GITHUB_LAST_BACKUP_EPOCH_SEC =
        longPreferencesKey("github_backup_last_backup_time_epoch_sec")

    val TELEGRAM_USER_ID_KEY = stringPreferencesKey("telegram_user_id")

    val TELEGRAM_BOT_ID_KEY = stringPreferencesKey("telegram_bot_id")

    val IS_TELEGRAM_BACKUP_ENABLED_KEY = booleanPreferencesKey("is_telegram_backup_enabled_key")

    val TELEGRAM_BACKUP_REPEAT_TIME_KEY = longPreferencesKey("telegram_backup_repeat_time_key")


    fun ivyFeature(key: String): Preferences.Key<Boolean> {
        return booleanPreferencesKey("feature_$key")
    }
}
