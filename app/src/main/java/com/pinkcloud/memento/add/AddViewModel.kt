package com.pinkcloud.memento.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao

class AddViewModel(val database: MemoDatabaseDao, application: Application): AndroidViewModel(application) {


}