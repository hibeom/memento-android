package com.pinkcloud.memento

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pinkcloud.memento.utils.Configuration
import com.pinkcloud.memento.utils.Constants

class SharedViewModel(val mApplication: Application): AndroidViewModel(mApplication) {
    val searchText = MutableLiveData("")

    val fontSizeLevel = MutableLiveData(Configuration.fontSizeLevel)
    val fontType = MutableLiveData(Configuration.fontType)

    val orderBy = MutableLiveData(Constants.ORDER_BY_PRIORITY)

    private val sharedPref: SharedPreferences = mApplication.getSharedPreferences(
        mApplication.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    init {
        orderBy.value = sharedPref.getInt(Constants.ORDER_BY, Constants.ORDER_BY_PRIORITY)
    }

    fun changeSearchText(text: String) {
        searchText.value = text
    }

    fun reduceFont() {
        if (fontSizeLevel.value == 0) return
        val newVal = fontSizeLevel.value!!.minus(1)
        Configuration.fontSizeLevel = newVal
        fontSizeLevel.value = newVal
        saveFontSize()
    }

    fun enlargeFont() {
        if (fontSizeLevel.value == 4) return
        val newVal = fontSizeLevel.value!!.plus(1)
        Configuration.fontSizeLevel = newVal
        fontSizeLevel.value = newVal
        saveFontSize()
    }

    fun changeFont() {
        val newVal = fontType.value!!.plus(1) % Constants.FONT_COUNT
        Configuration.fontType = newVal
        fontType.value = newVal
        saveFontType()
    }

    private fun saveFontSize() {
        with(sharedPref.edit()) {
            putInt(Constants.FONT_SIZE, fontSizeLevel.value!!)
            apply()
        }
    }

    private fun saveFontType() {
        with(sharedPref.edit()) {
            putInt(Constants.FONT_TYPE, fontType.value!!)
            apply()
        }
    }

    fun changeOrderBy() {
        orderBy.value = orderBy.value!!.plus(1) % 3
        with(sharedPref.edit()) {
            putInt(Constants.ORDER_BY, orderBy.value!!)
            apply()
        }
    }
}