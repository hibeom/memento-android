package com.pinkcloud.memento.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.net.toFile
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Constants for keys and ids
 * */
object Constants {
    const val KEY_TEMP_IMAGE_PATH = "temp_image_path"
    const val TEMP_FILE_NAME = "temp.jpg"
    const val CHANNEL_ID = "memo_alarm_channel"
    const val MEMO_ID = "memo_id"
    const val FRONT_CAPTION = "front_caption"
    const val LAST_NOTIFICATION_ID = "last_notification_id"
}

/**
 * Format time in millis to datetime string
 * like 2021/8/4 11:47 PM
 *
 * @return formatted datetime string
 * */
fun formatMillisToDatetime(timeMillis: Long): String {
    val instant = Instant.ofEpochMilli(timeMillis)
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

    val year = date.year
    val month = date.monthValue
    val day = date.dayOfMonth
    val hour = String.format("%02d", date.hour % 12)
    val minute = String.format("%02d", date.minute)
    val ampm = if (date.hour < 12) "AM" else "PM"

    return "${year}/${month}/${day} ${hour}:${minute} $ampm"
}

/**
 * Schedule alarm of a memo
 * @param context instance for WorkManager
 * @param memoId time in millis when the memo is created.
 * @param frontCaption  caption that will be shown when the scheduled notification is generated.
 * @param alarmTime the time when alarm is scheduled in millis.
 *
 * @return uuid string generated from Worker
 * */
fun scheduleAlarm(context: Context, memoId: Long, frontCaption: String?, alarmTime: Long): String {
    val data = Data.Builder()
    data.putLong(Constants.MEMO_ID, memoId)
    data.putString(Constants.FRONT_CAPTION, frontCaption)

    var delay = alarmTime - System.currentTimeMillis()
    delay = if (delay >= 0) delay else 0
    val work = OneTimeWorkRequestBuilder<NotificationWorker>().setInputData(data.build())
        .setInitialDelay(delay, TimeUnit.MILLISECONDS).build()

    WorkManager.getInstance(context).enqueue(work)
    return work.id.toString()
}

fun cancelAlarm(context: Context, alarmId: String) {
    val uuid = UUID.fromString(alarmId)
    WorkManager.getInstance(context).cancelWorkById(uuid)
}
