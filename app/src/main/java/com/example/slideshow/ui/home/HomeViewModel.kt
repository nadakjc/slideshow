package com.example.slideshow.ui.home

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.slideshow.SlideshowApp
import com.example.slideshow.data.SettingsRepository
import com.example.slideshow.data.model.SlideshowSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: SettingsRepository = (app as SlideshowApp).settingsRepository

    val settings: StateFlow<SlideshowSettings> = repo.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SlideshowSettings(),
    )

    fun onFolderPicked(uri: Uri) {
        val resolver = getApplication<Application>().contentResolver
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        releasePersisted(except = uri)
        runCatching { resolver.takePersistableUriPermission(uri, flags) }
        viewModelScope.launch { repo.setFolderUri(uri.toString()) }
    }

    private fun releasePersisted(except: Uri) {
        val resolver = getApplication<Application>().contentResolver
        resolver.persistedUriPermissions
            .map { it.uri }
            .filter { it != except }
            .forEach {
                runCatching {
                    resolver.releasePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
            }
    }
}
