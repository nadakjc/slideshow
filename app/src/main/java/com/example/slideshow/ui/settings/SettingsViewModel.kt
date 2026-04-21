package com.example.slideshow.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.slideshow.SlideshowApp
import com.example.slideshow.data.SettingsRepository
import com.example.slideshow.data.model.SlideshowSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: SettingsRepository = (app as SlideshowApp).settingsRepository

    val settings: StateFlow<SlideshowSettings> = repo.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SlideshowSettings(),
    )

    fun setIntervalMs(value: Long) {
        viewModelScope.launch { repo.setIntervalMs(value) }
    }

    fun setShuffle(value: Boolean) {
        viewModelScope.launch { repo.setShuffle(value) }
    }

    fun setLoop(value: Boolean) {
        viewModelScope.launch { repo.setLoop(value) }
    }

    fun setRecursive(value: Boolean) {
        viewModelScope.launch { repo.setRecursive(value) }
    }
}
