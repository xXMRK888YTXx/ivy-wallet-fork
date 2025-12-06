package com.xxmrk888ytxx.telegrambackup.model

sealed interface TelegramBackupState {
    object Loading : TelegramBackupState

    data class EnterTelegramData(
        val userId: String = "",
        val botToken: String = "",
        val isSaveButtonEnabled: Boolean = false
    ) : TelegramBackupState

    data class BackupConfiguration(val backupSettings: BackupSettings = BackupSettings()) : TelegramBackupState
}
