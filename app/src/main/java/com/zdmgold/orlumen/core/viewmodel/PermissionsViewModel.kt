package com.zdmgold.orlumen.core.viewmodel

import androidx.lifecycle.ViewModel
import com.zdmgold.orlumen.core.model.PermissionKind
import com.zdmgold.orlumen.platform.permissions.PermissionChecker
import com.zdmgold.orlumen.platform.permissions.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Permissions the user still has to grant, in the order the onboarding flow should ask for them. */
fun missingPermissions(state: PermissionState): List<PermissionKind> = buildList {
    if (!state.recordAudio) add(PermissionKind.MICROPHONE)
    if (!state.overlay) add(PermissionKind.OVERLAY)
    if (!state.postNotifications) add(PermissionKind.NOTIFICATIONS)
    if (!state.notificationAccess) add(PermissionKind.NOTIFICATION_ACCESS)
    if (!state.usageAccess) add(PermissionKind.USAGE_ACCESS)
    if (!state.batteryUnrestricted) add(PermissionKind.BATTERY)
}

/** The edge visualizer cannot run without audio access and the overlay permission. */
fun requiredPermissionsGranted(state: PermissionState): Boolean = state.recordAudio && state.overlay

@HiltViewModel
class PermissionsViewModel @Inject constructor(private val checker: PermissionChecker) : ViewModel() {
    private val _state = MutableStateFlow(checker.snapshot())
    val state: StateFlow<PermissionState> = _state.asStateFlow()

    /** Call when the screen resumes, because the user may have just changed a setting. */
    fun refresh() {
        _state.value = checker.snapshot()
    }
}
