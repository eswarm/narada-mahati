package `in`.eswarm.mahati.share

import androidx.compose.runtime.Composable

@Composable
fun QrScannerScreen(
    onScanResult: (String) -> Unit
) {
    QrCodeScanner(onScanResult = onScanResult)
}
