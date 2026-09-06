package com.example.flexkeypad.domain.repository

import com.example.flexkeypad.domain.model.KeypadProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfiles(): Flow<List<KeypadProfile>>
    fun getActiveProfile(): Flow<KeypadProfile>
    suspend fun setActiveProfileId(profileId: String)
    suspend fun saveProfile(profile: KeypadProfile)
    suspend fun createProfile(name: String): KeypadProfile
    suspend fun deleteProfile(profileId: String)
    suspend fun exportProfileToJson(profile: KeypadProfile): String
    suspend fun importProfileFromJson(jsonString: String): Result<KeypadProfile>
}
