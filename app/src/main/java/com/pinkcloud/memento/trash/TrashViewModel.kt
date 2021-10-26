package com.pinkcloud.memento.trash

import android.app.Application
import androidx.lifecycle.*
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import com.pinkcloud.memento.repository.MemoRepository
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.applyFilter
import com.pinkcloud.memento.utils.applySort
import com.pinkcloud.memento.utils.koreanmatcher.KoreanTextMatcher
import com.pinkcloud.memento.utils.scheduleAlarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class TrashViewModel(val database: MemoDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val memoRepository = MemoRepository(database)

    private val _orderBy = MutableStateFlow(Constants.ORDER_BY_PRIORITY)

    private val orderBy: StateFlow<Int>
        get() = _orderBy

    private val completedMemos = memoRepository.memos.mapLatest { memos ->
        memos.applyFilter(completed = true)
    }

    val memos: LiveData<List<Memo>> = orderBy
        .combine(completedMemos) { orderBy, memos ->
            memos.applySort(orderBy)
        }
        .asLiveData()

    fun restoreMemo(memo: Memo) {
        viewModelScope.launch {
            memo.isCompleted = false
            if (memo.isAlarmEnabled) {
                memo.alarmId = scheduleAlarm(getApplication(), memo.memoId, memo.frontCaption, memo.alarmTime, memo.imagePath)
            }
            database.update(memo)
        }
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch {
            database.delete(memo)
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