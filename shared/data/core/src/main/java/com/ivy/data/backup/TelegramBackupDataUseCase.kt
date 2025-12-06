package com.ivy.data.backup

import android.content.Context
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ivy.data.datastore.DatastoreKeys.LAST_BACKUP_HASH_KEY
import com.ivy.data.remote.TelegramDataSourceFactory
import com.ivy.data.repository.TelegramDataRepository
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TelegramBackupDataUseCase @Inject constructor(
    private val backupDataUseCase: BackupDataUseCase,
    private val telegramBackupRepository: TelegramDataRepository,
    private val telegramDataSourceFactory: TelegramDataSourceFactory,
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {

    private val telegramBackupFolder by lazy { context.getDir("telegram_backup", Context.MODE_PRIVATE) }

    suspend fun makeTelegramBackup():Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            clearBackupData()
            telegramBackupFolder.mkdir()
            val telegramUserId = telegramBackupRepository.userId.first()
            val telegramBotKey = telegramBackupRepository.botKey.first()
            if (telegramUserId == null || telegramBotKey == null) error("Invalid telegram data")
            val telegramDataSource = telegramDataSourceFactory.createDataSource(telegramUserId, telegramBotKey)

            val backupFile = File(telegramBackupFolder, provideBackupFileName())
            backupDataUseCase.exportToFile(backupFile.toUri())

            telegramDataSource.uploadFile(backupFile)
                .getOrThrow()

        }.also { clearBackupData() }
    }

    private suspend fun clearBackupData() {
        if (telegramBackupFolder.exists()) telegramBackupFolder.deleteRecursively()
    }

    private fun provideBackupFileName() : String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy  HH-mm")
        val formattedDateTime = currentDateTime.format(formatter)
        return "telegram_backup_$formattedDateTime.zip"
    }
}