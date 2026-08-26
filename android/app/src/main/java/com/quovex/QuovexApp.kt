package com.quovex

import android.app.Application
import com.quovex.data.worker.MorningBriefingWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuovexApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MorningBriefingWorker.schedule(this)
    }
}
