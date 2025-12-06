package com.ivy.data.remote.impl

import com.ivy.data.remote.TelegramDataSource
import com.ivy.data.remote.TelegramDataSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import javax.inject.Inject

class TelegramDataSourceFactoryImpl @Inject constructor(
    private val ktorClient: dagger.Lazy<HttpClient>,
    @param:ApplicationContext private val context: android.content.Context
) : TelegramDataSourceFactory() {
    override suspend fun createDataSource(
        userId: String,
        botKey: String
    ): TelegramDataSource = TelegramDataSourceImpl(userId, botKey, ktorClient.get(),context)
}