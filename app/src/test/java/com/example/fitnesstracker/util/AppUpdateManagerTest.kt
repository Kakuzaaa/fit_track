package com.example.fitnesstracker.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun isNewerVersion_detectsNewPatchVersion() {
        assertTrue(AppUpdateManager.isNewerVersion("1.4.2", "1.4.1"))
        assertTrue(AppUpdateManager.isNewerVersion("v1.4.2", "1.4.1"))
        assertTrue(AppUpdateManager.isNewerVersion("v1.4.2", "v1.4.1"))
    }

    @Test
    fun isNewerVersion_detectsNewMinorAndMajorVersion() {
        assertTrue(AppUpdateManager.isNewerVersion("1.5.0", "1.4.9"))
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0", "1.9.9"))
    }

    @Test
    fun isNewerVersion_handlesDoubleDigitNumbers() {
        assertTrue(AppUpdateManager.isNewerVersion("1.4.10", "1.4.2"))
        assertFalse(AppUpdateManager.isNewerVersion("1.4.2", "1.4.10"))
    }

    @Test
    fun isNewerVersion_returnsFalseForSameOrOlderVersion() {
        assertFalse(AppUpdateManager.isNewerVersion("1.4.1", "1.4.1"))
        assertFalse(AppUpdateManager.isNewerVersion("v1.4.1", "1.4.1"))
        assertFalse(AppUpdateManager.isNewerVersion("1.4.0", "1.4.1"))
        assertFalse(AppUpdateManager.isNewerVersion("1.3.9", "1.4.1"))
    }
}
