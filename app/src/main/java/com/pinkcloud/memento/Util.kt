package com.pinkcloud.memento

import java.text.DateFormat


object Constants {
    const val KEY_TEMP_IMAGE_PATH = "temp_image_path"
    const val TEMP_FILE_NAME = "temp.jpg"
}

fun formatMillisToDatetime(timeMillis: Long): String {
    return DateFormat.getDateInstance().format(timeMillis)
}