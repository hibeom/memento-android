package com.pinkcloud.memento.trash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.koreanmatcher.KoreanTextMatcher
import com.pinkcloud.memento.utils.scheduleAlarm
import kotlinx.coroutines.launch

class TrashViewModel(val database: MemoDatabaseDao, application: Application) : AndroidViewModel(application) {

    val orderBy = MutableLiveData(Constants.ORDER_BY_PRIORITY)
    val memos = Transformations.switchMap(orderBy) {
        when (it) {
            Constants.ORDER_BY_PRIORITY -> database.getCompletedMemos()
            Constants.ORDER_BY_NEWEST -> database.getCompletedMemosByDate(false)
            Constants.ORDER_BY_OLDEST -> database.getCompletedMemosByDate(true)
            else -> database.getCompletedMemos()
        }
    }

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
}