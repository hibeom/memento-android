package com.pinkcloud.memento.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.net.toFile
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.ZoneId


object Constants {
    const val KEY_TEMP_IMAGE_PATH = "temp_image_path"
    const val TEMP_FILE_NAME = "temp.jpg"
}

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
