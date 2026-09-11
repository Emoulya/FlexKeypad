package com.emoulya.flexkeypad.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.emoulya.flexkeypad.domain.repository.AppSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "flexkeypad_app_settings")

class DataStoreAppSettingsRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AppSettingsRepository {

    companion object {
        private val KEY_IS_FULLSCREEN = booleanPreferencesKey("is_fullscreen")
        private val KEY_IS_HAPTIC_ENABLED = booleanPreferencesKey("is_haptic_enabled")
    }

    override val isFullScreen: Flow<Boolean> = context.appSettingsDataStore.data
        .map { preferences ->
            preferences[KEY_IS_FULLSCREEN] ?: false
        }
        .distinctUntilChanged()

    override val isHapticEnabled: Flow<Boolean> = context.appSettingsDataStore.data
        .map { preferences ->
            preferences[KEY_IS_HAPTIC_ENABLED] ?: true
        }
        .distinctUntilChanged()

    override suspend fun setFullScreen(enabled: Boolean): Unit = withContext(ioDispatcher) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[KEY_IS_FULLSCREEN] = enabled
        }
    }

    override suspend fun setHapticEnabled(enabled: Boolean): Unit = withContext(ioDispatcher) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[KEY_IS_HAPTIC_ENABLED] = enabled
        }
    }
}
