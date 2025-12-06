package com.ivy.data.remote

abstract class TelegramDataSourceFactory {
    abstract suspend fun createDataSource(userId: String, botKey: String): TelegramDataSource
}