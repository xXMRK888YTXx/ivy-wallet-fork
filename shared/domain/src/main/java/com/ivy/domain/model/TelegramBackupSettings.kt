package com.ivy.domain.model

data class TelegramBackupSettings(
    val isEnabled: Boolean,
    val telegramBackupRepeatTime: TelegramBackupRepeatTime
)
