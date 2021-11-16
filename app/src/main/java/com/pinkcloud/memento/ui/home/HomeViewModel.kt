package com.pinkcloud.memento.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import com.pinkcloud.memento.repository.MemoRepository
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.applyFilter
import com.pinkcloud.memento.utils.applySort
import com.pinkcloud.memento.utils.cancelAlarm
import com.pinkcloud.memento.utils.koreanmatcher.KoreanTextMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MemoRepository,
    application: Application
) :
    AndroidViewModel(application) {

    private val _orderBy = MutableStateFlow(Constants.ORDER_BY_PRIORITY)

    private val orderBy: StateFlow<Int>
        get() = _orderBy

    private val unCompletedMemos = repository.memos.mapLatest { memos ->
        memos.applyFilter(completed = false)
    }

    val memos: LiveData<List<Memo>> = orderBy
        .combine(unCompletedMemos) { orderBy, memos ->
            memos.applySort(orderBy)
        }
        .asLiveData()

    fun completeMemo(memo: Memo) {
        viewModelScope.launch {
            memo.isCompleted = true
            if (memo.isAlarmEnabled) {
                memo.alarmId?.let {
                    cancelAlarm(getApplication(), it)
                }
            }
            memo.completedTime = System.currentTimeMillis()
            repository.updateMemo(memo)
        }
    }

    fun getFilteredMemos(text: String): List<Memo>? {
        return memos.value?.filter {
            val lowerText = text.lowercase()
            val frontResult = it.frontCaption?.lowercase()?.contains(lowerText) ?: false
            val backResult = it.backCaption?.lowercase()?.contains(lowerText) ?: false
            val frontResultKorean = KoreanTextMatcher.match(it.frontCaption, lowerText).success()
            val backResultKorean = KoreanTextMatcher.match(it.backCaption, lowerText).success()
            frontResult || backResult || frontResultKorean || backResultKorean
        }
    }

    fun onOrderChanged(order: Int) {
        _orderBy.value = order
    }
}