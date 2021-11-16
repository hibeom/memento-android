package com.pinkcloud.memento.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

private const val ONE_WEEK_IN_MILLI = 7*24*60*60*1000

@Dao
interface MemoDatabaseDao {

    @Insert
    suspend fun insert(memo: Memo)

    @Update
    suspend fun update(memo: Memo)

    @Delete
    suspend fun delete(memo: Memo)

    @Query("SELECT * FROM memo_table WHERE memo_id = :memoId")
    fun getMemo(memoId: Long): Flow<Memo>

    fun getMemoDistinct(memoId: Long) = getMemo(memoId)
        .distinctUntilChanged()

    @Query("SELECT * FROM memo_table")
    fun getAllMemo(): Flow<List<Memo>>

    @Query("DELETE FROM memo_table WHERE is_completed = 1 AND :currentTimeInMillis - completed_time >= $ONE_WEEK_IN_MILLI")
    fun deleteOldCompletedMemos(currentTimeInMillis: Long)

    @Query("SELECT * FROM memo_table WHERE is_completed = 1 AND :currentTimeInMillis - completed_time >= $ONE_WEEK_IN_MILLI")
    fun getOldCompletedMemos(currentTimeInMillis: Long): List<Memo>
}