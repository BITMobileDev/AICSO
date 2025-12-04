import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.aicso.component.PermissionRationaleDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestVoiceCallPermissions(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val permissionState = rememberPermissionState(
        permission = android.Manifest.permission.RECORD_AUDIO
    )

    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> {
                onPermissionGranted()
            }
            permissionState.status.shouldShowRationale -> {
                // Show rationale
            }
        }
    }

    if (permissionState.status.shouldShowRationale) {
        PermissionRationaleDialog(
            message = "This app needs access to your microphone to enable voice calls with the AI assistant.",
            onDismiss = onPermissionDenied,
            onConfirm = { permissionState.launchPermissionRequest() }
        )
    } else {
        LaunchedEffect(Unit) {
            permissionState.launchPermissionRequest()
        }
    }
}