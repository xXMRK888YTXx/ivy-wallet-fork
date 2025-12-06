package com.ivy.wallet

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.ivy.data.backup.TelegramBackupDataUseCase
import com.ivy.data.workManager.TelegramBackupWorkerNotificationProvider
import com.ivy.data.workManager.worker.TelegramBackupWorker
import javax.inject.Inject

class IvyWorkerFactory @Inject constructor(
    private val hiltWorkerFactory: HiltWorkerFactory,
    private val telegramBackupDataUseCase: TelegramBackupDataUseCase,
    private val telegramBackupWorkerNotificationProvider: TelegramBackupWorkerNotificationProvider
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

        else -> hiltWorkerFactory.createWorker(appContext, workerClassName, workerParameters)
    }
}