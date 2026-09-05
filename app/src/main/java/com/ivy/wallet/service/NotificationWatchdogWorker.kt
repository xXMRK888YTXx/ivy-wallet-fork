package com.ivy.wallet.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ivy.domain.NotificationParserController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NotificationWatchdogWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val notificationParserController: NotificationParserController
) : CoroutineWorker(appContext, workerParams) {

    constructor(
        appContext: Context,
        workerParams: WorkerParameters
    ) : this(
        appContext,
        workerParams,
        EntryPointAccessors.fromApplication(
            appContext,
            NotificationWatchdogWorkerEntryPoint::class.java
        ).notificationParserController()
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationWatchdogWorkerEntryPoint {
        fun notificationParserController(): NotificationParserController
    }

    override suspend fun doWork(): Result {
        Timber.d("NotificationWatchdogWorker: Running periodic watchdog check (30 min interval)")
        notificationParserController.checkAndRebindIfNeeded()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "ivy_notification_watchdog_work"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<NotificationWatchdogWorker>(
                30, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Timber.d("NotificationWatchdogWorker: Scheduled periodic work every 30 minutes")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Timber.d("NotificationWatchdogWorker: Cancelled periodic work")
        }
    }
}
