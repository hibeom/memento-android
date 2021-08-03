package com.pinkcloud.memento.trash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import kotlinx.coroutines.launch

class TrashViewModel(val database: MemoDatabaseDao, application: Application) : AndroidViewModel(application) {

    val memos = database.getCompletedMemos()

    fun restoreMemo(memo: Memo) {
        viewModelScope.launch {
            memo.isCompleted = true
            database.update(memo)
        }
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch {
            database.delete(memo)
        }
    }
}