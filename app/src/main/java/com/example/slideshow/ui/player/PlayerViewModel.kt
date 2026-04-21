package com.example.slideshow.ui.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.slideshow.R
import com.example.slideshow.SlideshowApp
import com.example.slideshow.data.FolderRepository
import com.example.slideshow.data.SettingsRepository
import com.example.slideshow.data.model.SlideshowSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val images: List<Uri> = emptyList(),
    val index: Int = 0,
    val isPlaying: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
) {
    val current: Uri? get() = images.getOrNull(index)
}

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo: SettingsRepository =
        (app as SlideshowApp).settingsRepository
    private val folderRepo = FolderRepository(app.applicationContext)

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var config: SlideshowSettings = SlideshowSettings()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val current = settingsRepo.settings.first()
        config = current
        val folderUri = current.folderUri?.let(Uri::parse)
        if (folderUri == null) {
            _state.update {
                it.copy(loading = false, error = getString(R.string.player_no_folder))
            }
            return
        }
        val images = folderRepo.listImages(folderUri, current.recursive)
        val ordered = if (current.shuffle) images.shuffled() else images
        if (ordered.isEmpty()) {
            _state.update {
                it.copy(
                    loading = false,
                    error = getString(R.string.player_empty),
                    images = emptyList(),
                )
            }
            return
        }
        _state.update {
            it.copy(
                images = ordered,
                index = 0,
                isPlaying = true,
                loading = false,
                error = null,
            )
        }
        restartTimer()
    }

    fun togglePlay() {
        val playing = !_state.value.isPlaying
        _state.update { it.copy(isPlaying = playing) }
        if (playing) restartTimer() else timerJob?.cancel()
    }

    fun next() = advance(+1)

    fun previous() = advance(-1)

    private fun advance(step: Int) {
        val s = _state.value
        if (s.images.isEmpty()) return
        val last = s.images.lastIndex
        val raw = s.index + step
        val newIndex = when {
            raw in 0..last -> raw
            config.loop -> ((raw % s.images.size) + s.images.size) % s.images.size
            else -> s.index.coerceIn(0, last)
        }
        if (newIndex == s.index) return
        _state.update { it.copy(index = newIndex) }
        if (s.isPlaying) restartTimer()
    }

    private fun restartTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.isPlaying) {
                delay(config.intervalMs)
                val s = _state.value
                if (!s.isPlaying || s.images.isEmpty()) break
                val atEnd = s.index >= s.images.lastIndex
                if (atEnd && !config.loop) {
                    _state.update { it.copy(isPlaying = false) }
                    break
                }
                val next = if (atEnd) 0 else s.index + 1
                _state.update { it.copy(index = next) }
            }
        }
    }

    private fun getString(resId: Int): String =
        getApplication<Application>().getString(resId)

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
