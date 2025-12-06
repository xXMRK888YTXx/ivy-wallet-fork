package com.ivy.data.workManager.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ivy.data.backup.TelegramBackupDataUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class TelegramBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val telegramBackupDataUseCase: TelegramBackupDataUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val backupResult = telegramBackupDataUseCase.makeTelegramBackup()
            .onFailure { Timber.e(it) }


        return if (backupResult.isSuccess) Result.success() else Result.retry()
    }
}