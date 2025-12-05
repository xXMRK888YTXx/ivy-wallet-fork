package com.ivy.domain.usecase.telegram

import com.ivy.data.repository.TelegramDataRepository
import com.ivy.domain.model.TelegramData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ManageTelegramDataUseCase @Inject constructor(
    private val telegramDataRepository: TelegramDataRepository
) {

    val telegramData: Flow<TelegramData?> = combine(
        telegramDataRepository.userId,
        telegramDataRepository.botKey
    ) { userId, botKey ->
        if (userId == null || botKey == null) return@combine null
        TelegramData(userId, botKey)
    }

    suspend fun setupTelegramData(telegramData: TelegramData) {
        telegramDataRepository.writeTelegramData(telegramData.userId, telegramData.botToken)
    }

    suspend fun removeTelegramData() {
        telegramDataRepository.removeTelegramData()
    }
}