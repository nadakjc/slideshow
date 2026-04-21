package com.example.slideshow

import android.app.Application
import com.example.slideshow.data.SettingsRepository

class SlideshowApp : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
    }
}
