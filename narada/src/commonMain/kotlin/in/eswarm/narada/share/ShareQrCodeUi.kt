package `in`.eswarm.narada.share

import androidx.compose.runtime.Composable

@Composable
expect fun ShareQrCodeDialog(
    onDismissRequest: () -> Unit,
    connectionDetailsJson: String
)
