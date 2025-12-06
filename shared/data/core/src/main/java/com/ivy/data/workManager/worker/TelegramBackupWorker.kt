package com.ivy.data.workManager.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.ivy.data.backup.TelegramBackupDataUseCase
import com.ivy.data.exception.ApiDataException
import com.ivy.data.workManager.TelegramBackupWorkerNotificationProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class TelegramBackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val telegramBackupDataUseCase: TelegramBackupDataUseCase,
    private val telegramBackupWorkerNotificationProvider: TelegramBackupWorkerNotificationProvider
) : CoroutineWorker(context, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            FOREGROUND_NOTIFICATION_ID,
            telegramBackupWorkerNotificationProvider.foregroundNotification
        )
    }

    override suspend fun doWork(): Result {
        val isSingleWork = inputData.getBoolean(IS_SINGLE_WORK_KEY,false)

        val backupResult = telegramBackupDataUseCase.makeTelegramBackup()
            .onFailure {
                Timber.e(it)

                when(it) {
                    is ApiDataException -> telegramBackupWorkerNotificationProvider.sendNotification("Telegram data is invalid. Please update it.")
                }
            }


        return if (backupResult.isSuccess) Result.success() else if (isSingleWork) Result.failure() else Result.retry()
    }

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1188

        const val IS_SINGLE_WORK_KEY = "IS_SINGLE_WORK"

    }
}