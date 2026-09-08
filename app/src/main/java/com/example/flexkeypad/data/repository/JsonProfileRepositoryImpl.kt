package com.example.flexkeypad.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.flexkeypad.domain.model.KeypadProfile
import com.example.flexkeypad.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flexkeypad_settings")

class JsonProfileRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProfileRepository {

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val activeProfileKey = stringPreferencesKey("active_profile_id")
    private val profilesDirectory = File(context.filesDir, "profiles")

    private val _profilesFlow = MutableStateFlow<List<KeypadProfile>>(emptyList())

    init {
        scope.launch {
            if (!profilesDirectory.exists()) {
                profilesDirectory.mkdirs()
            }
            loadAllProfilesFromDisk()
        }
    }

    private fun loadAllProfilesFromDisk() {
        val files = profilesDirectory.listFiles { file -> file.extension == "json" }
        val loadedProfiles = mutableListOf<KeypadProfile>()

        files?.forEach { file ->
            try {
                val content = file.readText()
                val profile = json.decodeFromString<KeypadProfile>(content)
                loadedProfiles.add(profile)
            } catch (_: Exception) {}
        }

        if (loadedProfiles.isEmpty()) {
            // PRD P0: Initial state is an empty canvas ready for customization
            val defaultProfile = KeypadProfile(
                profileId = "profile_default",
                profileName = "Default Keypad",
                buttons = emptyList()
            )
            val file = File(profilesDirectory, "${defaultProfile.profileId}.json")
            file.writeText(json.encodeToString(defaultProfile))
            loadedProfiles.add(defaultProfile)
        }

        _profilesFlow.value = loadedProfiles
    }

    override fun getProfiles(): Flow<List<KeypadProfile>> = _profilesFlow.asStateFlow()

    override fun getActiveProfile(): Flow<KeypadProfile> {
        return combine(
            _profilesFlow,
            context.dataStore.data.map { prefs -> prefs[activeProfileKey] }
        ) { profiles, activeId ->
            val found = profiles.find { it.profileId == activeId }
            found ?: profiles.firstOrNull() ?: KeypadProfile("default", "Default", emptyList())
        }.distinctUntilChanged()
    }

    override suspend fun setActiveProfileId(profileId: String): Unit = withContext(ioDispatcher) {
        context.dataStore.edit { prefs ->
            prefs[activeProfileKey] = profileId
        }
        Unit
    }


    override suspend fun saveProfile(profile: KeypadProfile) = withContext(ioDispatcher) {
        val file = File(profilesDirectory, "${profile.profileId}.json")
        val content = json.encodeToString(profile)
        file.writeText(content)

        val currentList = _profilesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.profileId == profile.profileId }
        if (index >= 0) {
            currentList[index] = profile
        } else {
            currentList.add(profile)
        }
        _profilesFlow.value = currentList
    }

    override suspend fun createProfile(name: String): KeypadProfile = withContext(ioDispatcher) {
        val newProfile = KeypadProfile(
            profileId = "prof_${UUID.randomUUID().toString().take(8)}",
            profileName = name.ifBlank { "Untitled Profile" },
            buttons = emptyList()
        )
        saveProfile(newProfile)
        setActiveProfileId(newProfile.profileId)
        newProfile
    }

    override suspend fun deleteProfile(profileId: String) = withContext(ioDispatcher) {
        if (_profilesFlow.value.size <= 1) {
            // Keep at least one profile
            return@withContext
        }
        val file = File(profilesDirectory, "$profileId.json")
        if (file.exists()) {
            file.delete()
        }
        val updated = _profilesFlow.value.filterNot { it.profileId == profileId }
        _profilesFlow.value = updated
        setActiveProfileId(updated.first().profileId)
    }

    override suspend fun exportProfileToJson(profile: KeypadProfile): String = withContext(ioDispatcher) {
        json.encodeToString(profile)
    }

    override suspend fun importProfileFromJson(jsonString: String): Result<KeypadProfile> = withContext(ioDispatcher) {
        try {
            val imported = json.decodeFromString<KeypadProfile>(jsonString)
            // Ensure unique ID on import if collision occurs
            val safeProfile = if (_profilesFlow.value.any { it.profileId == imported.profileId }) {
                imported.copy(profileId = "prof_${UUID.randomUUID().toString().take(8)}")
            } else {
                imported
            }
            saveProfile(safeProfile)
            Result.success(safeProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
