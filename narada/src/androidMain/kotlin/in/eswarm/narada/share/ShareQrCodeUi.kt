package `in`.eswarm.narada.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import io.github.alexzhirkevich.qrose.QrData
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
actual fun ShareQrCodeDialog(
    onDismissRequest: () -> Unit,
    connectionDetailsJson: String
) {

    /*
    val qrCode = createQrVector(
        data = QrData.Text(connectionDetailsJson),
        options = createQrVectorOptions {
            colors {
                dark = QrVectorColor.Solid(0xFF000000.toInt())
                ball = QrVectorColor.Solid(0xFF000000.toInt())
                frame = QrVectorColor.Solid(0xFF000000.toInt())
            }
        }
    )

     */

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Share Server Credentials") },
        text = {
            Box(modifier = Modifier.fillMaxSize()) {

                Image(
                    painter = rememberQrCodePainter(connectionDetailsJson),
                    contentDescription = "QR code referring to the example.com website"
                )

                /*
                Image(
                    bitmap = qrCode.asImageBitmap(),
                    contentDescription = "QR Code with server credentials"
                )

                 */
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Dismiss")
            }
        }
    )
}
