package com.ivy.data.repository

import com.ivy.data.remote.TelegramDataSourceFactory
import javax.inject.Inject

class TelegramRepository @Inject constructor(
    private val telegramDataSourceFactory: TelegramDataSourceFactory
) {
    suspend fun isValidData(userId: String,botKey: String): Result<Unit> {
        val dataSource = telegramDataSourceFactory.createDataSource(userId,botKey)
        return dataSource.sendMessage("Test message")
    }
}