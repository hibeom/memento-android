package com.pinkcloud.memento.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo_table")
data class Memo(
    @PrimaryKey
    @ColumnInfo(name = "memo_id")
    var memoId: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "image_path")
    var imagePath: String?,

    @ColumnInfo(name = "front_caption")
    var frontCaption: String?,

    @ColumnInfo(name = "back_caption")
    var backCaption: String?,

    @ColumnInfo(name = "priority")
    var priority: Int,

    @ColumnInfo(name = "alarm_time")
    var alarmTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_alarm_enabled")
    var isAlarmEnabled: Boolean = false,

    /**
     * The time when a memo is edited is given to alarmId.
     * alarmId will be used for canceling alarm from alarmManager.
     * */
    @ColumnInfo(name = "alarm_id")
    var alarmId: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_completed")
    var isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_time")
    var completedTime: Long?
)
