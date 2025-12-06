package com.ivy.domain.usecase.telegram

import com.ivy.data.exception.DataIvyException
import com.ivy.data.repository.TelegramRepository
import com.ivy.domain.exception.UnknownException
import com.ivy.domain.exception.toDomainException
import com.ivy.domain.model.TelegramData
import javax.inject.Inject

class TelegramUseCase @Inject constructor(
    private val telegramRepository: TelegramRepository
) {

    suspend fun validateTelegramData(telegramData: TelegramData): Result<Unit> = runCatching {
        try {
            telegramRepository.isValidData(telegramData.userId, telegramData.botToken).getOrThrow()
        } catch (e: DataIvyException) {
            throw e.toDomainException()
        } catch (e: Exception) {
            throw UnknownException(e.message)
        }
    }
}