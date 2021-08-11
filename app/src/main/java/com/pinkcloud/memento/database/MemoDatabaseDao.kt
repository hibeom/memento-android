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
    @Query("SELECT * FROM memo_table WHERE is_completed = 0 ORDER BY priority DESC")
    fun getOngoingMemos(): LiveData<List<Memo>>

    @Query("SELECT * FROM memo_table WHERE is_completed = 1 ORDER BY priority DESC")
    fun getCompletedMemos(): LiveData<List<Memo>>
    
    @Query("DELETE FROM memo_table WHERE is_completed = 1 AND :currentTimeInMillis - completed_time >= 7*24*60*60*1000")
    fun deleteOldCompletedMemos(currentTimeInMillis: Long)

    @Query("SELECT * FROM memo_table WHERE is_completed = 1 AND :currentTimeInMillis - completed_time >= 7*24*60*60*1000")
    fun getOldCompletedMemos(currentTimeInMillis: Long): LiveData<List<Memo>>
}