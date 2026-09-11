package com.emoulya.flexkeypad

import com.emoulya.flexkeypad.domain.model.KeypadButton
import com.emoulya.flexkeypad.domain.model.KeypadProfile
import com.emoulya.flexkeypad.domain.repository.ProfileRepository
import com.emoulya.flexkeypad.domain.usecase.ManageProfileUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeProfileRepository : ProfileRepository {
    val profiles = mutableListOf<KeypadProfile>()
    private val profilesFlow = MutableStateFlow<List<KeypadProfile>>(emptyList())
    private val activeProfileFlow = MutableStateFlow(KeypadProfile("default", "Default Profile", emptyList()))
    var lastActiveProfileId: String = "default"

    init {
        val initial = KeypadProfile("default", "Default Profile", emptyList())
        profiles.add(initial)
        profilesFlow.value = profiles.toList()
        activeProfileFlow.value = initial
    }

    override fun getProfiles(): Flow<List<KeypadProfile>> = profilesFlow

    override fun getActiveProfile(): Flow<KeypadProfile> = activeProfileFlow

    override suspend fun setActiveProfileId(profileId: String) {
        lastActiveProfileId = profileId
        val found = profiles.find { it.profileId == profileId }
        if (found != null) {
            activeProfileFlow.value = found
        }
    }

    override suspend fun saveProfile(profile: KeypadProfile) {
        val index = profiles.indexOfFirst { it.profileId == profile.profileId }
        if (index >= 0) {
            profiles[index] = profile
        } else {
            profiles.add(profile)
        }
        profilesFlow.value = profiles.toList()
        if (profile.profileId == lastActiveProfileId) {
            activeProfileFlow.value = profile
        }
    }

    override suspend fun deleteProfile(profileId: String) {
        profiles.removeAll { it.profileId == profileId }
        profilesFlow.value = profiles.toList()
    }

    override suspend fun createProfile(name: String): KeypadProfile {
        val newProfile = KeypadProfile(
            profileId = "profile_${System.currentTimeMillis()}",
            profileName = name,
            buttons = emptyList()
        )
        saveProfile(newProfile)
        return newProfile
    }

    private val json = Json { prettyPrint = true }

    override suspend fun exportProfileToJson(profile: KeypadProfile): String {
        return json.encodeToString(KeypadProfile.serializer(), profile)
    }

    override suspend fun importProfileFromJson(jsonString: String): Result<KeypadProfile> {
        return runCatching {
            val parsed = json.decodeFromString(KeypadProfile.serializer(), jsonString)
            saveProfile(parsed)
            parsed
        }
    }
}

class ManageProfileUseCaseTest {

    private lateinit var fakeRepository: FakeProfileRepository
    private lateinit var useCase: ManageProfileUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeProfileRepository()
        useCase = ManageProfileUseCase(fakeRepository)
    }

    @Test
    fun createProfile_addsProfileToRepository() = runBlocking {
        val created = useCase.createProfile("Gaming Profile")
        assertEquals("Gaming Profile", created.profileName)
        assertTrue(fakeRepository.profiles.any { it.profileId == created.profileId })
    }

    @Test
    fun deleteProfile_removesProfileFromRepository() = runBlocking {
        val profile = useCase.createProfile("To Delete")
        assertTrue(fakeRepository.profiles.any { it.profileId == profile.profileId })

        useCase.deleteProfile(profile.profileId)
        assertFalse(fakeRepository.profiles.any { it.profileId == profile.profileId })
    }

    @Test
    fun addButton_addsNewButtonToProfileAndSaves() = runBlocking {
        val defaultProfile = fakeRepository.profiles.first()
        val updated = useCase.addButton(
            currentProfile = defaultProfile,
            label = "TEST",
            posX = 50f,
            posY = 60f,
            width = 120f,
            height = 80f,
            hidKeyCode = 4 // 'A'
        )

        assertEquals(1, updated.buttons.size)
        val btn = updated.buttons.first()
        assertEquals("TEST", btn.label)
        assertEquals(50f, btn.positionX)
        assertEquals(60f, btn.positionY)
        assertEquals(4, btn.hidKeyCode)
    }

    @Test
    fun updateButton_modifiesMatchingButton() = runBlocking {
        val defaultProfile = fakeRepository.profiles.first()
        val profileWithBtn = useCase.addButton(defaultProfile, label = "OLD")
        val originalBtn = profileWithBtn.buttons.first()

        val modifiedBtn = originalBtn.copy(label = "NEW", positionX = 200f)
        val updatedProfile = useCase.updateButton(profileWithBtn, modifiedBtn)

        val retrievedBtn = updatedProfile.buttons.first()
        assertEquals("NEW", retrievedBtn.label)
        assertEquals(200f, retrievedBtn.positionX)
    }

    @Test
    fun duplicateButton_createsOffsetCopy() = runBlocking {
        val defaultProfile = fakeRepository.profiles.first()
        val profileWithBtn = useCase.addButton(defaultProfile, label = "ORIGINAL", posX = 100f, posY = 100f)
        val originalBtn = profileWithBtn.buttons.first()

        val duplicated = useCase.duplicateButton(
            currentProfile = profileWithBtn,
            sourceButton = originalBtn,
            offsetX = 20f,
            offsetY = 30f
        )

        assertNotEquals(originalBtn.id, duplicated.id)
        assertEquals(originalBtn.label, duplicated.label)
        assertEquals(120f, duplicated.positionX)
        assertEquals(130f, duplicated.positionY)
    }

    @Test
    fun deleteButton_removesSpecifiedButtonOnly() = runBlocking {
        var profile = fakeRepository.profiles.first()
        profile = useCase.addButton(profile, label = "BTN 1")
        profile = useCase.addButton(profile, label = "BTN 2")
        assertEquals(2, profile.buttons.size)

        val btn1Id = profile.buttons[0].id
        val updated = useCase.deleteButton(profile, btn1Id)

        assertEquals(1, updated.buttons.size)
        assertEquals("BTN 2", updated.buttons.first().label)
    }

    @Test
    fun exportAndImportProfile_preservesDataAndSetsActive() = runBlocking {
        var profile = useCase.createProfile("Exportable")
        profile = useCase.addButton(profile, label = "KEY X", hidKeyCode = 27)

        val exportedJson = useCase.exportProfile(profile)
        assertNotNull(exportedJson)
        assertTrue(exportedJson.contains("KEY X"))

        val importResult = useCase.importProfile(exportedJson)
        assertTrue(importResult.isSuccess)
        val imported = importResult.getOrThrow()
        assertEquals(profile.profileId, imported.profileId)
        assertEquals("Exportable", imported.profileName)
        assertEquals(1, imported.buttons.size)
        assertEquals(fakeRepository.lastActiveProfileId, imported.profileId)
    }

    @Test
    fun importProfile_invalidJson_returnsFailure() = runBlocking {
        val result = useCase.importProfile("INVALID_JSON_STRING")
        assertTrue(result.isFailure)
    }
}
