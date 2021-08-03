package com.pinkcloud.memento

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.RefreshWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MementoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        createNotificationChannel()
        enqueueDailyWork()
    }

    private fun enqueueDailyWork() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        val work = PeriodicWorkRequestBuilder<RefreshWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            Constants.DAILY_REFRESH_WORK, ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}