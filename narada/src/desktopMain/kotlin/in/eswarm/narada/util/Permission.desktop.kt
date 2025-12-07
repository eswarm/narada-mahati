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