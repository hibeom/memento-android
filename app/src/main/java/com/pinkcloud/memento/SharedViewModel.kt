package com.pinkcloud.memento

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class SharedViewModel: ViewModel() {
    val searchText = MutableLiveData("")

    fun changeSearchText(text: String) {
        searchText.value = text
    }
}