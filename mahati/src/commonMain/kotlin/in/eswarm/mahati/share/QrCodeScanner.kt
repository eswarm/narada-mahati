package `in`.eswarm.mahati.share

import androidx.compose.runtime.Composable

@Composable
expect fun QrCodeScanner(onScanResult: (String) -> Unit)
