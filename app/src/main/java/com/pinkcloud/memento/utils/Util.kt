package com.pinkcloud.memento.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.media.ExifInterface
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pinkcloud.memento.R
import com.pinkcloud.memento.ui.common.MemoView
import com.pinkcloud.memento.database.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
    const val IMAGE_PATH = "image_path"

    const val LAST_NOTIFICATION_ID = "last_notification_id"
    const val DAILY_REFRESH_WORK = "daily_refresh_work"

    const val FONT_SIZE = "font_size"
    const val DEFAULT_FONT_SIZE = 2
    const val FONT_TYPE = "font_type"
    const val DEFAULT_FONT = 0
    const val FONT_NANUM_BARUNPEN = 1
    const val FONT_NANUM_BRUSH = 2
    const val FONT_NANUM_PEN = 3
    const val FONT_COUNT = 4

    const val ORDER_BY = "order_by"
    const val ORDER_BY_PRIORITY = 0
    const val ORDER_BY_NEWEST = 1
    const val ORDER_BY_OLDEST = 2

    const val FLASH_MODE = "flash_mode"
}

/**
 * Application Configuration
 *
 * fontSizeLevel 0-4
 * fontType 0: DEFAULT - 3: NANUM PEN
 * */
object Configuration {
    var fontSizeLevel: Int = 2
    var fontType: Int = 0
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
    val lang = Locale.getDefault().language

    val year = date.year
    val month = date.monthValue
    val day = date.dayOfMonth
    val hour = String.format("%02d", date.hour % 12)
    val minute = String.format("%02d", date.minute)

    // 'when' expression below exists because I can't approach local string resources in Util.kt
    // below codes can be replaced with getting string resources from app context.
    var amStr = "AM";
    var pmStr = "PM"
    when (lang) {
        Locale.KOREA.language -> {
            amStr = "오전"; pmStr = "오후"
        }
    }
    val ampm = if (date.hour < 12) amStr else pmStr

    return when (lang) {
        Locale.KOREA.language -> "${year}-${month}-${day} $ampm ${hour}:${minute}"
        else -> "${year}-${month}-${day} ${hour}:${minute} $ampm"
    }
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
suspend fun scheduleAlarm(
    context: Context,
    memoId: Long,
    frontCaption: String?,
    alarmTime: Long,
    imagePath: String?
): String? {
    return withContext(Dispatchers.Default) {
        val data = Data.Builder()
        data.putLong(Constants.MEMO_ID, memoId)
        data.putString(Constants.FRONT_CAPTION, frontCaption)
        data.putString(Constants.IMAGE_PATH, imagePath)

        val delay = alarmTime - System.currentTimeMillis()
        if (delay <= 0) return@withContext null
        val work = OneTimeWorkRequestBuilder<NotificationWorker>().setInputData(data.build())
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).build()

        WorkManager.getInstance(context).enqueue(work)
        return@withContext work.id.toString()
    }
}

/**
 * Cancel scheduled alarm
 *
 * @param alarmId id generated by work
 * */
fun cancelAlarm(context: Context, alarmId: String) {
    val uuid = UUID.fromString(alarmId)
    WorkManager.getInstance(context).cancelWorkById(uuid)
}

/**
 * copy temp image to destination
 *
 * @param dstFileName memo image file for the created memo. (image+memoId.jpg)
 * @return memo image absolute path
 * */
fun copyTempImage(context: Context, dstFileName: String): String {
    val srcFile = File(
        context.filesDir,
        Constants.TEMP_FILE_NAME
    )
    val dstFile = File(context.filesDir, dstFileName)
    srcFile.copyTo(dstFile, true)
    return dstFile.absolutePath
}

/**
 * save empty black drawable image to an temp image file.
 * */
fun saveEmptyTempImage(context: Context) {
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.empty)
    val tempFile = File(
        context.filesDir,
        Constants.TEMP_FILE_NAME
    )
    saveBitmap(bitmap, tempFile)
}

fun saveViewImage(context: Context, bitmap: Bitmap) {
    val tempFile = File(
        context.filesDir,
        Constants.TEMP_FILE_NAME
    )
    saveBitmap(bitmap, tempFile)
}

fun saveBitmap(bitmap: Bitmap, dstFile: File) {
    val outputStream = FileOutputStream(dstFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
}

fun getTempFile(context: Context): File {
    return File(
        context.filesDir,
        Constants.TEMP_FILE_NAME
    )
}

/**
 * delete a saved image when memo is deleting completely.
 *
 * @param filePath absolute file path of an image of memo.
 * */
fun deleteImage(filePath: String?) {
    filePath?.let {
        val file = File(it)
        file.delete()
    }
}

/**
 * return correctly rotated portrait image if origin image has rotated orientation.
 *
 * @param imagePath absolute file path of an image of a memo.
 * @return correctly rotated portrait image.
 * */
fun getRotatedBitmap(imagePath: String?): Bitmap? {
    if (imagePath == null) return null

    val exif = ExifInterface(imagePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
    val srcBitmap = BitmapFactory.decodeFile(imagePath)

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }
    return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.width, srcBitmap.height, matrix, true)
}

/**
 * get measured font size by an each font type.
 * even when font has a same font size, its size looks different.
 * 16sp of Default font size looks bigger then 16sp of Nanum fonts.
 *
 * @return measured font size
 * */
fun getMeasuredFontSize(): Float {
    val measuredSizeInt = when (Configuration.fontType) {
        Constants.FONT_NANUM_BARUNPEN -> {
            18 + Configuration.fontSizeLevel
        }
        Constants.FONT_NANUM_BRUSH -> {
            25 + Configuration.fontSizeLevel
        }
        Constants.FONT_NANUM_PEN -> {
            24 + Configuration.fontSizeLevel
        }
        else -> {
            16 + Configuration.fontSizeLevel
        }
    }
    return measuredSizeInt.toFloat()
}

/**
 * get font name to be displayed on MenuSheetFragment
 *
 * @param context to get string resource
 * @return font name text
 * */
fun getFontName(context: Context): String {
    return when (Configuration.fontType) {
        Constants.FONT_NANUM_BARUNPEN -> context.getString(R.string.font_nanum_barunpen)
        Constants.FONT_NANUM_BRUSH -> context.getString(R.string.font_nanum_brush)
        Constants.FONT_NANUM_PEN -> context.getString(R.string.font_nanum_pen)
        else -> context.getString(R.string.default_font)
    }
}

/**
 * get font family resource by fontType
 *
 * @param fontType 0: DEFATUL, 1: NANUM BARUNPEN, 2: NANUM BRUSH, 3: NANUM PEN
 * @return font family resource
 * */
fun getFontFamily(fontType: Int): Int? {
    return when (fontType) {
        Constants.FONT_NANUM_BARUNPEN -> R.font.nanum_barunpen
        Constants.FONT_NANUM_BRUSH -> R.font.nanum_brush
        Constants.FONT_NANUM_PEN -> R.font.nanum_pen
        else -> null
    }
}

/**
 * get typeface of a fontType
 *
 * @return DEFAULT typeface if font family is null
 * */
fun getTypeface(context: Context): Typeface {
    val fontFamily = getFontFamily(Configuration.fontType)
    var typeface = Typeface.DEFAULT
    fontFamily?.let {
        typeface = context.resources.getFont(it)
    }
    return typeface
}

fun shareMemo(context: Context, memoView: MemoView) {
    val bitmap = memoView.drawToBitmap()
    saveViewImage(context, bitmap)
    val uriToImage = FileProvider.getUriForFile(
        context,
        "com.pinkcloud.memento.fileprovider",
        getTempFile(context)
    )
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uriToImage)
        type = "image/jpeg"
    }
    shareIntent.clipData = ClipData.newRawUri("", uriToImage)
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.resources.getText(R.string.app_name)
        )
    )
}

fun hideKeyboard(context: Context, v: View) {
    (context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(v.windowToken, 0)
    }
}

/**
 * An extension function of List<Memo>
 * apply sort by order flag on default thread.
 */
suspend fun List<Memo>.applySort(orderBy: Int): List<Memo> =
    withContext(Dispatchers.Default) {
        when (orderBy) {
            Constants.ORDER_BY_PRIORITY -> sortedByDescending { memo ->
                memo.priority
            }
            Constants.ORDER_BY_OLDEST -> sortedBy { memo ->
                memo.memoId
            }
            Constants.ORDER_BY_NEWEST -> sortedByDescending { memo ->
                memo.memoId
            }
            else -> this@applySort
        }
    }

/**
 * An extension function of List<Memo>
 * filter completed/uncompleted memos on default thread.
 */
suspend fun List<Memo>.applyFilter(completed: Boolean): List<Memo> =
    withContext(Dispatchers.Default) {
        this@applyFilter.filter { memo ->
            memo.isCompleted == completed
        }
    }