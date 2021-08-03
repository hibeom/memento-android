package com.pinkcloud.memento.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import kotlinx.coroutines.launch

class AddViewModel(val database: MemoDatabaseDao, application: Application) :
    AndroidViewModel(application) {

    var isInsertCompleted = MutableLiveData<Boolean>(false)

    fun insertMemo(
        imagePath: String,
        frontCaption: String?,
        backCaption: String?,
        priority: Int,
        alarmTime: Long,
        isAlarmEnabled: Boolean
    ) {
        viewModelScope.launch {
            TODO("Set alarm and insert memo")
            if (isAlarmEnabled) {
                // set alarm
            }
            val memo = Memo(System.currentTimeMillis(), imagePath, frontCaption, backCaption, priority, alarmTime, isAlarmEnabled, )
            isInsertCompleted.value = true
        }
    }
}