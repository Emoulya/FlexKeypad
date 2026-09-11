package com.emoulya.flexkeypad.domain.usecase

import com.emoulya.flexkeypad.domain.model.KeypadButton
import com.emoulya.flexkeypad.domain.model.KeypadProfile
import com.emoulya.flexkeypad.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ManageProfileUseCase(
    private val repository: ProfileRepository
) {
    fun getProfiles(): Flow<List<KeypadProfile>> = repository.getProfiles()

    fun getActiveProfile(): Flow<KeypadProfile> = repository.getActiveProfile()

    suspend fun selectProfile(profileId: String) {
        repository.setActiveProfileId(profileId)
    }

    suspend fun createProfile(name: String): KeypadProfile {
        return repository.createProfile(name)
    }

    suspend fun deleteProfile(profileId: String) {
        repository.deleteProfile(profileId)
    }

    suspend fun addButton(
        currentProfile: KeypadProfile,
        label: String = "KEY",
        posX: Float = 100f,
        posY: Float = 100f,
        width: Float = 140f,
        height: Float = 100f,
        hidKeyCode: Int = 0
    ): KeypadProfile {
        val newButton = KeypadButton(
            id = "btn_${UUID.randomUUID().toString().take(8)}",
            label = label,
            positionX = posX,
            positionY = posY,
            width = width,
            height = height,
            hidKeyCode = hidKeyCode
        )
        val updatedProfile = currentProfile.copy(
            buttons = currentProfile.buttons + newButton
        )
        repository.saveProfile(updatedProfile)
        return updatedProfile
    }

    suspend fun updateButton(
        currentProfile: KeypadProfile,
        updatedButton: KeypadButton
    ): KeypadProfile {
        val updatedButtons = currentProfile.buttons.map {
            if (it.id == updatedButton.id) updatedButton else it
        }
        val updatedProfile = currentProfile.copy(buttons = updatedButtons)
        repository.saveProfile(updatedProfile)
        return updatedProfile
    }

    suspend fun duplicateButton(
        currentProfile: KeypadProfile,
        sourceButton: KeypadButton,
        offsetX: Float = 24f,
        offsetY: Float = 24f
    ): KeypadButton {
        val newButton = sourceButton.copy(
            id = "btn_${UUID.randomUUID().toString().take(8)}",
            positionX = (sourceButton.positionX + offsetX).coerceAtLeast(0f),
            positionY = (sourceButton.positionY + offsetY).coerceAtLeast(0f)
        )
        val updatedProfile = currentProfile.copy(
            buttons = currentProfile.buttons + newButton
        )
        repository.saveProfile(updatedProfile)
        return newButton
    }

    suspend fun deleteButton(
        currentProfile: KeypadProfile,
        buttonId: String
    ): KeypadProfile {
        val updatedButtons = currentProfile.buttons.filterNot { it.id == buttonId }
        val updatedProfile = currentProfile.copy(buttons = updatedButtons)
        repository.saveProfile(updatedProfile)
        return updatedProfile
    }

    suspend fun exportProfile(profile: KeypadProfile): String {
        return repository.exportProfileToJson(profile)
    }

    suspend fun importProfile(jsonString: String): Result<KeypadProfile> {
        val result = repository.importProfileFromJson(jsonString)
        if (result.isSuccess) {
            val imported = result.getOrThrow()
            repository.setActiveProfileId(imported.profileId)
        }
        return result
    }
}
