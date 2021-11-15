package com.pinkcloud.memento.ui.edit

import android.app.Application
import androidx.lifecycle.*
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class EditViewModel @AssistedInject constructor(
    private val database: MemoDatabaseDao,
    application: Application,
    @Assisted memoId: Long,
) :
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

    companion object {
        fun provideFactory(
            assistedFactory: EditViewModelFactory,
            memoId: Long
        ): ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(memoId) as T
            }
        }
    }
}