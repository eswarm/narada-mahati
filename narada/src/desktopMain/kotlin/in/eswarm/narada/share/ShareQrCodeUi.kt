package `in`.eswarm.narada.share

import androidx.compose.runtime.Composable

@Composable
actual fun ShareQrCodeDialog(
    onDismissRequest: () -> Unit,
    connectionDetailsJson: String
) {
    // QR code sharing is not supported on desktop
}
