package com.pinkcloud.memento

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import timber.log.Timber
import java.io.File
import java.text.DateFormat


object Constants {
    const val KEY_TEMP_IMAGE_PATH = "temp_image_path"
    const val TEMP_FILE_NAME = "temp.jpg"
}

fun formatMillisToDatetime(timeMillis: Long): String {
    return DateFormat.getDateInstance().format(timeMillis)
}

fun copyGalleryImage(context: Context, srcUri: Uri) {
    // photoFile-> /data/user/0/com.pinkcloud.memento/files/temp.jpg
    // savedUri-> file:///data/user/0/com.pinkcloud.memento/files/temp.jpg

    val dstFile = File(
        context.filesDir,
        Constants.TEMP_FILE_NAME
    )
//    val savedUri = Uri.fromFile(dstFile)
    val projection = arrayOf(MediaStore.Images.Media._ID)
//    Timber.d(srcUri.toFile().absolutePath) // IllegarArgumentException: Uri lacks 'file' scheme
    context.contentResolver.query(srcUri, projection, null, null, null)?.use { cursor ->
        TODO("Implement save content uri to temp.jpg")
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        while (cursor.moveToNext()) {
            val contentUri = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                cursor.getString(idColumn)
            )
            Timber.d(contentUri.toString())
            contentUri.toFile().copyTo(dstFile, true)
        }
    }
}