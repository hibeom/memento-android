package com.pinkcloud.memento.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import com.pinkcloud.memento.utils.scheduleAlarm
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
            val memoId = System.currentTimeMillis()
            var alarmId: String? = null
            if (isAlarmEnabled) {
                alarmId = scheduleAlarm(getApplication(), memoId, frontCaption, alarmTime)
            }
            val memo = Memo(memoId, imagePath, frontCaption, backCaption, priority, alarmTime, isAlarmEnabled, alarmId, false, null)
            database.insert(memo)
            isInsertCompleted.value = true
        }
    }
}