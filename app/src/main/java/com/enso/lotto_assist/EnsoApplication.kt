package com.enso.lotto_assist

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EnsoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}