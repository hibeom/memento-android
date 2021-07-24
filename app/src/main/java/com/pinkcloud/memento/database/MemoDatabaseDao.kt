package com.pinkcloud.memento.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MemoDatabaseDao {

    @Insert
    suspend fun insert(memo: Memo)

    @Update
    suspend fun update(memo: Memo)

    @Delete
    suspend fun delete(memo: Memo)

    // Query dao with returning LiveData is suspend fun as far as I know.
    @Query("SELECT * FROM memo_table WHERE is_completed = 0")
    fun getOngoingMemos(): LiveData<List<Memo>>

    @Query("SELECT * FROM memo_table WHERE is_completed = 1")
    fun getCompletedMemos(): LiveData<List<Memo>>
}