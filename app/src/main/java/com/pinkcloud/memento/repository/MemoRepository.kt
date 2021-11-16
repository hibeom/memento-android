package com.pinkcloud.memento.repository

import com.pinkcloud.memento.database.Memo
import com.pinkcloud.memento.database.MemoDatabaseDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // The annotation that scopes an instance to the application container.
class MemoRepository @Inject constructor(
    private val database: MemoDatabaseDao
) {

    val memos: Flow<List<Memo>>
        get() = database.getAllMemo()
            .flowOn(Dispatchers.IO)

    suspend fun updateMemo(memo: Memo) {
        withContext(Dispatchers.IO) {
            database.update(memo)
        }
    }

    suspend fun deleteMemo(memo: Memo) {
        withContext(Dispatchers.IO) {
            database.delete(memo)
        }
    }

    fun getMemo(id: Long) = database.getMemoDistinct(id)

    suspend fun insertMemo(memo: Memo) {
        withContext(Dispatchers.IO) {
            database.insert(memo)
        }
    }
}