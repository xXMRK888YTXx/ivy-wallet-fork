package com.ivy.domain.model

import com.ivy.data.repository.TelegramBackupSettingsRepository.Companion.DEFAULT_TELEGRAM_BACKUP_REPEAT_TIME_IN_MILLS

enum class TelegramBackupRepeatTime(val timeInMills: Long) {
    HOURS_6(DEFAULT_TELEGRAM_BACKUP_REPEAT_TIME_IN_MILLS),
    HOURS_12(43_200_000L),
    DAY_1(86_400_000L),
    WEEK_1(604_800_000L);

    companion object {
        val defaultValue: TelegramBackupRepeatTime
            get() = HOURS_6
    }
}