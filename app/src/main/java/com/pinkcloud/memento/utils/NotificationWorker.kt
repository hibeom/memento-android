package com.pinkcloud.memento.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pinkcloud.memento.MainActivity
import com.pinkcloud.memento.R
import java.io.File

class NotificationWorker(val context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {
        val memoId = inputData.getLong(Constants.MEMO_ID, 0L)
        val frontCaption = inputData.getString(Constants.FRONT_CAPTION)
        val imagePath = inputData.getString(Constants.IMAGE_PATH)

        val bundle = Bundle()
        bundle.putLong(Constants.MEMO_ID, memoId)

        val pendingIntent = NavDeepLinkBuilder(context).setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.homeFragment)
            .setArguments(bundle)
            .createPendingIntent()

        val bitmap = getRotatedBitmap(imagePath)

        val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(frontCaption)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setExtras(bundle)
            .setLargeIcon(bitmap)
            .setAutoCancel(true)

        val id = getNotificationId()
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
        return Result.success()
    }

    private fun getNotificationId(): Int {
        val sharedPref = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        val lastId = sharedPref.getInt(Constants.LAST_NOTIFICATION_ID, 0)
        val newId = lastId + 1
        with(sharedPref.edit()) {
            putInt(Constants.LAST_NOTIFICATION_ID, newId)
            apply()
        }
        return newId
    }
}