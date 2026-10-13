package com.zdmgold.orlumen.core.viewmodel

import com.zdmgold.orlumen.core.model.PermissionKind
import com.zdmgold.orlumen.platform.permissions.PermissionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionsViewModelTest {
    private fun state(
        audio: Boolean = true,
        overlay: Boolean = true,
        notificationAccess: Boolean = true,
        usage: Boolean = true,
        notifications: Boolean = true,
        battery: Boolean = true
    ) = PermissionState(audio, overlay, notificationAccess, usage, notifications, battery)

    @Test
    fun nothingIsMissingWhenEverythingIsGranted() {
        assertTrue(missingPermissions(state()).isEmpty())
    }

    @Test
    fun missingPermissionsComeInOnboardingOrder() {
        val none = state(false, false, false, false, false, false)
        assertEquals(
            listOf(
                PermissionKind.MICROPHONE,
                PermissionKind.OVERLAY,
                PermissionKind.NOTIFICATIONS,
                PermissionKind.NOTIFICATION_ACCESS,
                PermissionKind.USAGE_ACCESS,
                PermissionKind.BATTERY
            ),
            missingPermissions(none)
        )
    }

    @Test
    fun onlyUngrantedPermissionsAreListed() {
        assertEquals(listOf(PermissionKind.USAGE_ACCESS), missingPermissions(state(usage = false)))
    }

    @Test
    fun theVisualizerNeedsAudioAndOverlayOnly() {
        assertTrue(requiredPermissionsGranted(state(notificationAccess = false, usage = false, battery = false)))
        assertFalse(requiredPermissionsGranted(state(audio = false)))
        assertFalse(requiredPermissionsGranted(state(overlay = false)))
    }
}
