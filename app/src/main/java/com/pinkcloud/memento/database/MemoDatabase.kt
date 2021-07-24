package com.pinkcloud.memento.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Memo::class], version = 1, exportSchema = false)
abstract class MemoDatabase : RoomDatabase() {

    abstract val memoDatabaseDao: MemoDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: MemoDatabase? = null

        fun getInstance(context: Context): MemoDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MemoDatabase::class.java,
                        "memo_database"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }
    }
}