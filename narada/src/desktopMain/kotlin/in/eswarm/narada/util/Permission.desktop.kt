package `in`.eswarm.narada.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPermissionState(permission: String): PermissionState {
    return DesktopPermissionState()
}

class DesktopPermissionState : PermissionState {
    override val status: PermissionStatus
        get() = PermissionStatus.GRANTED

    override fun launchPermissionRequest() {
        // NOOP
    }
}

actual val postNotificationPermission: String
    get() = "desktop.notification.permission"

actual object PlatformUtil {
    actual val isNotificationPermissionRequired: Boolean
        get() = false
}
