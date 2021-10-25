package com.pinkcloud.memento.home

import android.app.Application
import androidx.lifecycle.*
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import com.pinkcloud.memento.utils.Constants
import com.pinkcloud.memento.utils.cancelAlarm
import com.pinkcloud.memento.utils.koreanmatcher.KoreanTextMatcher
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class HomeViewModel(val database: MemoDatabaseDao, application: Application) :
    AndroidViewModel(application) {

    private val _orderBy = MutableLiveData(Constants.ORDER_BY_PRIORITY)

    private val orderBy: LiveData<Int>
        get() = _orderBy

    val memos = orderBy.switchMap {
        when (it) {
            Constants.ORDER_BY_PRIORITY -> database.getOngoingMemos()
            Constants.ORDER_BY_NEWEST -> database.getOngoingMemosByDate(false)
            Constants.ORDER_BY_OLDEST -> database.getOngoingMemosByDate(true)
            else -> database.getOngoingMemos()
        }
    }

    fun completeMemo(memo: Memo) {
        viewModelScope.launch {
            memo.isCompleted = true
            if (memo.isAlarmEnabled) {
                memo.alarmId?.let {
                    cancelAlarm(getApplication(), it)
                }
            }
            memo.completedTime = System.currentTimeMillis()
            database.update(memo)
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