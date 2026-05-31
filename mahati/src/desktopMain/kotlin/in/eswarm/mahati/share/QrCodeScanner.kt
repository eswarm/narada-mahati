package `in`.eswarm.mahati.share

import androidx.compose.runtime.Composable

@Composable
actual fun QrCodeScanner(onScanResult: (String) -> Unit) {
    // QR code scanning is not supported on desktop
}
