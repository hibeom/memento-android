package com.pinkcloud.memento.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pinkcloud.memento.database.MemoDatabase

class RefreshWorker(val context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        val dataSource = MemoDatabase.getInstance(context).memoDatabaseDao
        dataSource.deleteOldCompletedMemos(System.currentTimeMillis())

        return Result.success()
    }
}