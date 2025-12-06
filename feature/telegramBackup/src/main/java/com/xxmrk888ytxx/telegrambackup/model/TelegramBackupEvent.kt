package com.xxmrk888ytxx.telegrambackup.model

sealed interface TelegramBackupEvent {
    data class UserIdTextFieldChanged(val text: String) : TelegramBackupEvent

    data class BotTokenTextFieldChanged(val text: String) : TelegramBackupEvent

    object SaveNewTelegramData : TelegramBackupEvent

    data class ChangeTelegramBackupState(val isEnabled: Boolean) : TelegramBackupEvent

    data class ChangeBackupRepeatTimeEvent(val backupRepeatTime: BackupRepeatTime) : TelegramBackupEvent

    object RemoveTelegramDataEvent : TelegramBackupEvent

    object CreateBackupNowEvent : TelegramBackupEvent
}
