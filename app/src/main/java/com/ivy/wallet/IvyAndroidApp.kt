package com.ivy.wallet

import android.app.Application
import androidx.work.Configuration
import com.ivy.base.legacy.appContext
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

/**
 * Created by iliyan on 24.02.18.
 */
@HiltAndroidApp
class IvyAndroidApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: IvyWorkerFactory

    @Inject
    lateinit var notificationParserController: com.ivy.domain.NotificationParserController

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appContext = this

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        notificationParserController.syncState()
    }
}
