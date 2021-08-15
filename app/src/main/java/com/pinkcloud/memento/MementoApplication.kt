package com.pinkcloud.memento

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pinkcloud.memento.utils.Configuration
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
        setConfiguration()
    }

    private fun setConfiguration() {
        val sharedPref: SharedPreferences = getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        val fontSizeLevel = sharedPref.getInt(Constants.FONT_SIZE, Constants.DEFAULT_FONT_SIZE)
        val fontType = sharedPref.getInt(Constants.FONT_TYPE, Constants.DEFAULT_FONT)
        Configuration.fontSizeLevel = fontSizeLevel
        Configuration.fontType = fontType
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