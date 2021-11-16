package com.pinkcloud.memento.ui.edit

import android.app.Application
import androidx.lifecycle.*
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import com.pinkcloud.memento.repository.MemoRepository
import com.pinkcloud.memento.utils.cancelAlarm
import com.pinkcloud.memento.utils.scheduleAlarm
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EditViewModel @AssistedInject constructor(
    private val repository: MemoRepository,
    application: Application,
    @Assisted memoId: Long,
) :
    AndroidViewModel(application) {

    private val _isEditCompleted = MutableLiveData(false)

    val isEditCompleted: LiveData<Boolean>
        get() = _isEditCompleted

    private val _memo = repository.getMemo(memoId)
        .asLiveData()

    val memo: LiveData<Memo>
        get() = _memo

    fun onEditCompleted() {
        _isEditCompleted.value = false
    }

    companion object {
        fun provideFactory(
            assistedFactory: EditViewModelFactory,
            memoId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(memoId) as T
            }
        }
    }

    fun editMemo(memo: Memo, wasAlarmEnabled: Boolean) {
        viewModelScope.launch {
            rescheduleAlarm(memo, wasAlarmEnabled)
                .also { memo ->
                    repository.updateMemo(memo)
                    _isEditCompleted.value = true
                }
        }
    }

    private suspend fun rescheduleAlarm(memo: Memo, wasAlarmEnabled: Boolean): Memo {
        return memo.apply {
            if (wasAlarmEnabled) {
                if (isAlarmEnabled) {
                    alarmId?.let { cancelAlarm(getApplication(), it) }
                }
                val scheduledAlarmId =
                    scheduleAlarm(
                        getApplication(),
                        memoId,
                        frontCaption,
                        alarmTime,
                        imagePath
                    )
                alarmId = scheduledAlarmId
            } else {
                if (isAlarmEnabled) {
                    alarmId?.let { cancelAlarm(getApplication(), alarmId!!) }
                    alarmId = null
                }
            }
        }
    }
}