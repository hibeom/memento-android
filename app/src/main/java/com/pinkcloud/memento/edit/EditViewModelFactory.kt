package com.pinkcloud.memento.edit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pinkcloud.memento.database.MemoDatabaseDao

class EditViewModelFactory(
    private val memoId: Long,
    private val dataSource: MemoDatabaseDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
            return EditViewModel(memoId, dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}