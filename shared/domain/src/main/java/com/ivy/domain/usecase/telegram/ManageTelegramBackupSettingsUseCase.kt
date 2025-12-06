package com.ivy.domain.usecase.telegram

import com.ivy.data.repository.TelegramBackupRepository
import com.ivy.domain.model.TelegramBackupRepeatTime
import com.ivy.domain.model.TelegramBackupSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ManageTelegramBackupSettingsUseCase @Inject constructor(
    private val telegramBackupRepository: TelegramBackupRepository
) {

    val backupSettings: Flow<TelegramBackupSettings> = combine(telegramBackupRepository.isEnabled,telegramBackupRepository.telegramBackupRepeatTimeInMills) { isEnabled, telegramBackupRepeatTime ->
        TelegramBackupSettings(isEnabled, telegramBackupRepeatTime.toTelegramBackupRepeatTime())

    }

    suspend fun setEnableState(newState: Boolean) {
        telegramBackupRepository.enableBackup(newState)
    }

    suspend fun setBackupTime(telegramBackupRepeatTime: TelegramBackupRepeatTime) {
        telegramBackupRepository.changeTelegramBackupRepeatTime(telegramBackupRepeatTime.timeInMills)
    }

    suspend fun makeBackupNow() = telegramBackupRepository.runSingleBackupToTelegram()

    private fun Long.toTelegramBackupRepeatTime() : TelegramBackupRepeatTime {
        return TelegramBackupRepeatTime.entries.firstOrNull() { it.timeInMills == this } ?: error("Unknown telegram backup repeat time")
    }
}