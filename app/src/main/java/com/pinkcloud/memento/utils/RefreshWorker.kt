package com.pinkcloud.memento.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pinkcloud.memento.database.MemoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException

class RefreshWorker(val context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    override fun doWork(): Result {

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val dataSource = MemoDatabase.getInstance(context).memoDatabaseDao
                val currentTime = System.currentTimeMillis()

                val oldCompletedMemos = dataSource.getOldCompletedMemos(currentTime)
                oldCompletedMemos.forEach {
                    deleteImage(it.imagePath)
                }

                dataSource.deleteOldCompletedMemos(currentTime)
            } catch (exception: IOException) {
                cancel()
            }
        }

        return Result.success()
    }
}