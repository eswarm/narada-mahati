package `in`.eswarm.narada.util

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus as AccompanistPermissionStatus
import com.google.accompanist.permissions.rememberPermissionState as rememberAccompanistPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberPermissionState(permission: String): PermissionState {
    val permissionState = rememberAccompanistPermissionState(permission)
    return object : PermissionState {
        override val status: PermissionStatus
            get() = when (permissionState.status) {
                is AccompanistPermissionStatus.Granted -> PermissionStatus.GRANTED
                is AccompanistPermissionStatus.Denied -> PermissionStatus.DENIED
            }

        override fun launchPermissionRequest() {
            permissionState.launchPermissionRequest()
        }
    }
}

actual val postNotificationPermission: String
    get() = Manifest.permission.POST_NOTIFICATIONS

actual object PlatformUtil {
    actual val isNotificationPermissionRequired: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}
