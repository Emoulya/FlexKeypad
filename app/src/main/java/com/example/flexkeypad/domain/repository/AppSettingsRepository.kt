package com.example.flexkeypad.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val isFullScreen: Flow<Boolean>
    val isHapticEnabled: Flow<Boolean>

    suspend fun setFullScreen(enabled: Boolean)
    suspend fun setHapticEnabled(enabled: Boolean)
}
