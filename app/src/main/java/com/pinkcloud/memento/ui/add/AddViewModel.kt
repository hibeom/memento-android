package com.pinkcloud.memento.ui.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import com.pinkcloud.memento.repository.MemoRepository
import com.pinkcloud.memento.utils.copyTempImage
import com.pinkcloud.memento.utils.scheduleAlarm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    application: Application,
    private val repository: MemoRepository
) :
    AndroidViewModel(application) {

    private val _isInsertCompleted = MutableLiveData(false)

    val isInsertCompleted: LiveData<Boolean>
        get() = _isInsertCompleted

    fun insertMemo(
        frontCaption: String?,
        backCaption: String?,
        priority: Int,
        alarmTime: Long,
        isAlarmEnabled: Boolean
    ) {
        viewModelScope.launch {
            val memoId = System.currentTimeMillis()

            val imagePath = copyTempImage(getApplication(), "image$memoId.jpg")

            var alarmId: String? = null
            if (isAlarmEnabled) {
                alarmId =
                    scheduleAlarm(getApplication(), memoId, frontCaption, alarmTime, imagePath)
            }
            val memo = Memo(
                memoId,
                imagePath,
                frontCaption,
                backCaption,
                priority,
                alarmTime,
                isAlarmEnabled,
                alarmId,
                false,
                null
            )
            repository.insertMemo(memo)
            _isInsertCompleted.value = true
        }
    }

    fun onInsertCompleted() {
        _isInsertCompleted.value = false
    }
}