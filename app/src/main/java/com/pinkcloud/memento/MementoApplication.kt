package com.pinkcloud.memento

import android.app.Application
import timber.log.Timber

class MementoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}