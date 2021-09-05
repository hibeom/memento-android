package com.pinkcloud.memento.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import kotlinx.coroutines.launch

class EditViewModel(private val memoId: Long, val database: MemoDatabaseDao, application: Application) :
    AndroidViewModel(application) {

    val isEditCompleted = MutableLiveData(false)
    val memo = database.getMemo(memoId)

    fun updateMemo(memo: Memo) {
        viewModelScope.launch {
            database.update(memo)
            isEditCompleted.value = true
        }
    }
}