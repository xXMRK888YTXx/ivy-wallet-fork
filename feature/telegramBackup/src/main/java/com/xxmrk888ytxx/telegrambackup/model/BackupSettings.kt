package com.xxmrk888ytxx.telegrambackup.model

data class BackupSettings(
    val isEnabled: Boolean = false,
    val backupRepeatTime: BackupRepeatTime = BackupRepeatTime.defaultValue,
)