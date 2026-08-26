package com.quovex.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.BlockedAppInfo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DistractionBlockerRepositoryTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var packageManager: PackageManager
    private val memoryStorage = mutableMapOf<String, Any>()

    private lateinit var repository: DistractionBlockerRepositoryImpl

    @Before
    fun setUp() {
        memoryStorage.clear()
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        packageManager = mockk(relaxed = true)

        every { context.getSharedPreferences("quovex_distraction_shield_prefs", Context.MODE_PRIVATE) } returns prefs
        every { context.packageManager } returns packageManager
        every { context.packageName } returns "com.quovex"
        every { packageManager.queryIntentActivities(any(), any<Int>()) } returns emptyList()
        every { prefs.edit() } returns editor

        every { prefs.getBoolean("key_distraction_shield_enabled", any()) } answers {
            (memoryStorage["key_distraction_shield_enabled"] as? Boolean) ?: true
        }
        every { prefs.getStringSet("key_blocked_package_names", any()) } answers {
            (memoryStorage["key_blocked_package_names"] as? Set<String>) ?: emptySet()
        }

        every { editor.putBoolean(any(), any()) } answers {
            memoryStorage[firstArg()] = secondArg<Boolean>()
            editor
        }
        every { editor.putStringSet(any(), any()) } answers {
            memoryStorage[firstArg()] = secondArg<Set<String>>()
            editor
        }
        every { editor.apply() } returns Unit

        repository = DistractionBlockerRepositoryImpl(context)
    }

    @Test
    fun `initial shield state defaults to enabled`() {
        val state = repository.shieldState.value
        assertTrue(state.isShieldEnabled)
        assertEquals(0, state.totalAttemptsResistedToday)
    }

    @Test
    fun `setShieldEnabled updates state and preferences`() = runTest {
        repository.setShieldEnabled(false)
        assertFalse(repository.shieldState.value.isShieldEnabled)
        assertFalse(memoryStorage["key_distraction_shield_enabled"] as Boolean)

        repository.setShieldEnabled(true)
        assertTrue(repository.shieldState.value.isShieldEnabled)
    }

    @Test
    fun `recordDistractionAttempt increments package and total resisted count`() = runTest {
        val attempt1 = repository.recordDistractionAttempt("com.instagram.android")
        assertEquals(1, attempt1)
        assertEquals(1, repository.shieldState.value.totalAttemptsResistedToday)

        val attempt2 = repository.recordDistractionAttempt("com.instagram.android")
        assertEquals(2, attempt2)
        assertEquals(2, repository.shieldState.value.totalAttemptsResistedToday)

        val ytAttempt = repository.recordDistractionAttempt("com.google.android.youtube")
        assertEquals(1, ytAttempt)
        assertEquals(3, repository.shieldState.value.totalAttemptsResistedToday)
    }

    @Test
    fun `updateAccessibilityStatus updates state`() {
        repository.updateAccessibilityStatus(true)
        assertTrue(repository.shieldState.value.isAccessibilityServiceEnabled)

        repository.updateAccessibilityStatus(false)
        assertFalse(repository.shieldState.value.isAccessibilityServiceEnabled)
    }
}
