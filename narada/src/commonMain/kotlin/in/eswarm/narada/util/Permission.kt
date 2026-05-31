package `in`.eswarm.narada.util

import androidx.compose.runtime.Composable

enum class PermissionStatus {
    GRANTED,
    DENIED;

    val isGranted: Boolean
        get() = this == GRANTED
}

@Composable
expect fun rememberPermissionState(permission: String): PermissionState

interface PermissionState {
    val status: PermissionStatus
    fun launchPermissionRequest()
}

expect val postNotificationPermission: String

expect object PlatformUtil {
    val isNotificationPermissionRequired: Boolean
}
