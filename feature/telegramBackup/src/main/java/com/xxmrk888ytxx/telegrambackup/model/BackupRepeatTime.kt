package com.xxmrk888ytxx.telegrambackup.model

import com.ivy.domain.model.TelegramBackupRepeatTime

enum class BackupRepeatTime {
    HOURS_6,
    HOURS_12,
    DAY_1,
    WEEK_1;

    companion object {

        fun TelegramBackupRepeatTime.toBackupTime() : BackupRepeatTime = when(this) {
            TelegramBackupRepeatTime.HOURS_6 -> HOURS_6
            TelegramBackupRepeatTime.HOURS_12 -> HOURS_12
            TelegramBackupRepeatTime.DAY_1 -> DAY_1
            TelegramBackupRepeatTime.WEEK_1 -> WEEK_1
        }

        val defaultValue: BackupRepeatTime
            get() = TelegramBackupRepeatTime.defaultValue.toBackupTime()
    }
}