package com.pinkcloud.memento.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import kotlinx.coroutines.launch

class HomeViewModel(val database: MemoDatabaseDao, application: Application) : AndroidViewModel(application) {

    val memos = database.getOngoingMemos()

    fun completeMemo(memo: Memo) {
        viewModelScope.launch {
            memo.isCompleted = true
            database.update(memo)
        }
    }
}