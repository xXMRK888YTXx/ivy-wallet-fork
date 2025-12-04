package com.ivy.data.remote

interface TelegramDataSource {
    suspend fun sendMessage(text: String) : Result<Unit>
}