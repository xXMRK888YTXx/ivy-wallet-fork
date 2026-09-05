package com.ivy.wallet

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.ivy.data.backup.TelegramBackupDataUseCase
import com.ivy.data.workManager.TelegramBackupWorkerNotificationProvider
import com.ivy.data.workManager.worker.TelegramBackupWorker
import com.ivy.domain.NotificationParserController
import javax.inject.Inject
import javax.inject.Provider

class IvyWorkerFactory @Inject constructor(
    private val hiltWorkerFactory: HiltWorkerFactory,
    private val telegramBackupDataUseCase: TelegramBackupDataUseCase,
    private val telegramBackupWorkerNotificationProvider: TelegramBackupWorkerNotificationProvider,
    private val notificationParserController: Provider<NotificationParserController>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        TelegramBackupWorker::class.java.name -> TelegramBackupWorker(
            appContext,
            workerParameters,
            telegramBackupDataUseCase,
            telegramBackupWorkerNotificationProvider
        )

        com.ivy.wallet.service.NotificationWatchdogWorker::class.java.name ->
            com.ivy.wallet.service.NotificationWatchdogWorker(
                appContext,
                workerParameters,
                notificationParserController.get()
            )

        else -> hiltWorkerFactory.createWorker(appContext, workerClassName, workerParameters)
    }
}