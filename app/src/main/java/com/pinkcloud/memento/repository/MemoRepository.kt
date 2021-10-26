package com.pinkcloud.memento.repository

import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

class MemoRepository(private val database: MemoDatabaseDao) {

    val memos: Flow<List<Memo>>
        get() = database.getAllMemo()
            .flowOn(Dispatchers.IO)

}