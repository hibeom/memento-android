package com.pinkcloud.memento.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import kotlinx.coroutines.launch

class EditViewModel(private val memoId: Long, val database: MemoDatabaseDao, application: Application) :
    AndroidViewModel(application) {

    private val _isEditCompleted = MutableLiveData(false)

    val isEditCompleted: LiveData<Boolean>
        get() = _isEditCompleted

    private val _memo = database.getMemo(memoId)

    val memo: LiveData<Memo>
        get() = _memo

    fun updateMemo(memo: Memo) {
        viewModelScope.launch {
            database.update(memo)
            _isEditCompleted.value = true
        }
    }

    fun onEditCompleted() {
        _isEditCompleted.value = false
    }
}