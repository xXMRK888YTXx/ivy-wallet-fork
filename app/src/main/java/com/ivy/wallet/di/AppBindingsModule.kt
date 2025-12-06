package com.ivy.wallet.di

import com.ivy.data.workManager.TelegramBackupWorkerNotificationProvider
import com.ivy.domain.AppStarter
import com.ivy.wallet.IvyAppStarter
import com.ivy.wallet.workManager.TelegramBackupWorkerNotificationProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    abstract fun appStarter(appStarter: IvyAppStarter): AppStarter

    @Binds
    abstract fun telegramBackupWorkerNotificationProvider(impl: TelegramBackupWorkerNotificationProviderImpl): TelegramBackupWorkerNotificationProvider
}
