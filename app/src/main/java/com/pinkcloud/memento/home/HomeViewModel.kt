package com.pinkcloud.memento.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pinkcloud.memento.database.MemoDatabaseDao

class HomeViewModel(val database: MemoDatabaseDao, application: Application) : AndroidViewModel(application) {

    val memos = database.getOngoingMemos()
}