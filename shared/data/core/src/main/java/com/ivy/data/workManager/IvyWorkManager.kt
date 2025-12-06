package com.ivy.data.workManager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import com.ivy.data.workManager.worker.TelegramBackupWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class IvyWorkManager @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) {

    private val workManager by lazy { androidx.work.WorkManager.getInstance(context) }

    private val constraintsForTelegramBackupWorkers by lazy {
        Constraints(requiredNetworkType = NetworkType.CONNECTED)
    }

    internal fun runSingleBackupToTelegram() {
        val worker = OneTimeWorkRequestBuilder<TelegramBackupWorker>()
            .addTag(SINGLE_BACKUP_WORKER_NAME)
            .setConstraints(constraintsForTelegramBackupWorkers)
            .build()

        workManager.enqueueUniqueWork(
            SINGLE_BACKUP_WORKER_NAME,
            androidx.work.ExistingWorkPolicy.KEEP,
            worker
        )
    }

    internal fun enablePeriodicBackupToTelegramWorker(repeatTimeInMills: Long) {
        val workRequest = PeriodicWorkRequestBuilder<TelegramBackupWorker>(
            repeatTimeInMills,
            TimeUnit.MILLISECONDS
        )
            .addTag(PERIODIC_BACKUP_WORKER_NAME)
            .setConstraints(constraintsForTelegramBackupWorkers)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_BACKUP_WORKER_NAME,
            ExistingPeriodicWorkPolicy.UPDATE ,
            workRequest
        )
    }

    fun cancelPeriodicBackupToTelegramWorker() {
        workManager.cancelUniqueWork(PERIODIC_BACKUP_WORKER_NAME)
    }

    companion object {
        const val SINGLE_BACKUP_WORKER_NAME = "TelegramBackupWorker_single"

        const val PERIODIC_BACKUP_WORKER_NAME = "TelegramBackupWorker_periodic"

    }
}