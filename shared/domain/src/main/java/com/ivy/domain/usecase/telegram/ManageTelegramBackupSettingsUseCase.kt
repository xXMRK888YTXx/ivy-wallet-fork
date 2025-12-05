package com.ivy.domain.usecase.telegram

import com.ivy.data.repository.TelegramBackupSettingsRepository
import com.ivy.domain.model.TelegramBackupRepeatTime
import com.ivy.domain.model.TelegramBackupSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ManageTelegramBackupSettingsUseCase @Inject constructor(
    private val telegramBackupSettingsRepository: TelegramBackupSettingsRepository
) {

    val backupSettings: Flow<TelegramBackupSettings> = combine(telegramBackupSettingsRepository.isEnabled,telegramBackupSettingsRepository.telegramBackupRepeatTimeInMills) { isEnabled, telegramBackupRepeatTime ->
        TelegramBackupSettings(isEnabled, telegramBackupRepeatTime.toTelegramBackupRepeatTime())

    }

    suspend fun setEnableState(newState: Boolean) {
        telegramBackupSettingsRepository.writeEnabledState(newState)
    }

    suspend fun setBackupTime(telegramBackupRepeatTime: TelegramBackupRepeatTime) {
        telegramBackupSettingsRepository.writeTelegramBackupRepeatTime(telegramBackupRepeatTime.timeInMills)
    }

    private fun Long.toTelegramBackupRepeatTime() : TelegramBackupRepeatTime {
        return TelegramBackupRepeatTime.entries.firstOrNull() { it.timeInMills == this } ?: error("Unknown telegram backup repeat time")
    }
}