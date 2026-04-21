package com.example.slideshow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.slideshow.data.model.SlideshowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "slideshow_settings")

class SettingsRepository(private val appContext: Context) {

    private object Keys {
        val FOLDER_URI = stringPreferencesKey("folder_uri")
        val INTERVAL_MS = longPreferencesKey("interval_ms")
        val SHUFFLE = booleanPreferencesKey("shuffle")
        val LOOP = booleanPreferencesKey("loop")
        val RECURSIVE = booleanPreferencesKey("recursive")
    }

    val settings: Flow<SlideshowSettings> = appContext.dataStore.data.map { prefs ->
        SlideshowSettings(
            folderUri = prefs[Keys.FOLDER_URI],
            intervalMs = prefs[Keys.INTERVAL_MS] ?: 5_000L,
            shuffle = prefs[Keys.SHUFFLE] ?: false,
            loop = prefs[Keys.LOOP] ?: true,
            recursive = prefs[Keys.RECURSIVE] ?: true,
        )
    }

    suspend fun setFolderUri(uri: String?) {
        appContext.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(Keys.FOLDER_URI) else prefs[Keys.FOLDER_URI] = uri
        }
    }

    suspend fun setIntervalMs(value: Long) {
        appContext.dataStore.edit { it[Keys.INTERVAL_MS] = value }
    }

    suspend fun setShuffle(value: Boolean) {
        appContext.dataStore.edit { it[Keys.SHUFFLE] = value }
    }

    suspend fun setLoop(value: Boolean) {
        appContext.dataStore.edit { it[Keys.LOOP] = value }
    }

    suspend fun setRecursive(value: Boolean) {
        appContext.dataStore.edit { it[Keys.RECURSIVE] = value }
    }
}
