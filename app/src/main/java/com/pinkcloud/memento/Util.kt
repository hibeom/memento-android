package com.pinkcloud.memento

import java.text.DateFormat

fun formatMillisToDatetime(timeMillis: Long): String {
    return DateFormat.getDateInstance().format(timeMillis)
}