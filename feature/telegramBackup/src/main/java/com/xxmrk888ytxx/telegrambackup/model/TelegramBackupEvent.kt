package com.xxmrk888ytxx.telegrambackup.model

sealed interface TelegramBackupEvent {
    data class UserIdTextFieldChanged(val text: String) : TelegramBackupEvent

    data class BotTokenTextFieldChanged(val text: String) : TelegramBackupEvent

    object SaveNewTelegramData : TelegramBackupEvent
}