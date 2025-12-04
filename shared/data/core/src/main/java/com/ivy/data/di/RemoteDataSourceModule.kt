package com.ivy.data.di

import com.ivy.data.remote.RemoteExchangeRatesDataSource
import com.ivy.data.remote.TelegramDataSourceFactory
import com.ivy.data.remote.impl.RemoteExchangeRatesDataSourceImpl
import com.ivy.data.remote.impl.TelegramDataSourceFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {
    @Binds
    abstract fun bindExchangeRatesDataSource(
        datasource: RemoteExchangeRatesDataSourceImpl
    ): RemoteExchangeRatesDataSource

    @Binds
    abstract fun provideTelegramDataSourceFactory(
        telegramDataSourceFactoryImpl: TelegramDataSourceFactoryImpl
    ) : TelegramDataSourceFactory
}
