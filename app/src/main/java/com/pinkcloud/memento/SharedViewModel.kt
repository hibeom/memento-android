package com.pinkcloud.memento

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pinkcloud.memento.utils.Constants
import timber.log.Timber

class SharedViewModel(application: Application): AndroidViewModel(application) {
    val searchText = MutableLiveData("")
    val fontSize = MutableLiveData(Constants.DEFAULT_FONT_SIZE)
    private var sharedPref: SharedPreferences

    init {
        sharedPref = application.getSharedPreferences(
            application.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        fontSize.value = sharedPref.getInt(Constants.FONT_SIZE, Constants.DEFAULT_FONT_SIZE)
    }

    fun changeSearchText(text: String) {
        searchText.value = text
    }

    fun reduceFont() {
        if (fontSize.value == 14) return
        fontSize.value = fontSize.value!!.minus(1)
        saveFontSize()
    }

    fun enlargeFont() {
        if (fontSize.value == 20) return
        fontSize.value = fontSize.value!!.plus(1)
        saveFontSize()
    }

    private fun saveFontSize() {
        with(sharedPref.edit()) {
            putInt(Constants.FONT_SIZE, fontSize.value!!)
            apply()
        }
    }
}