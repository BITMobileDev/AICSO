import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.aicso.component.PermissionRationaleDialog
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestVideoCallPermissions(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    )

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            onPermissionsGranted()
        }
    }

    if (permissionsState.shouldShowRationale) {
        // Show rationale dialog
        PermissionRationaleDialog(
            message = "This app needs access to your camera and microphone to enable video calls with the AI assistant.",
            onDismiss = onPermissionsDenied,
            onConfirm = { permissionsState.launchMultiplePermissionRequest() }
        )
    } else {
        LaunchedEffect(Unit) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
}
