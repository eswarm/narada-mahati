package `in`.eswarm.mahati.home

import PermissionState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeDroidViewModel : ViewModel() {
    // Initialize with null to represent an undetermined state
    private val _permissionState = MutableStateFlow<PermissionState?>(null)
    val permissionState = _permissionState.asStateFlow()

    fun onPermissionGranted() {
        _permissionState.value = PermissionState.GRANTED
    }

    fun onPermissionRationale() {
        _permissionState.value = PermissionState.SHOW_RATIONALE
    }

    fun onPermissionDenied() {
        _permissionState.value = PermissionState.DENIED
    }
}
