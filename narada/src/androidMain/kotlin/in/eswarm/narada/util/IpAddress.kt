package `in`.eswarm.narada.util

import android.content.Context
import android.net.wifi.WifiManager
import java.util.Formatter

/**
 * Gets the local IP address of the device on the Wi-Fi network.
 */
actual fun getLocalIpAddress(): String? {
    val wifiManager = AppContext.context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val ipAddress = wifiManager.connectionInfo.ipAddress
    val formatter = Formatter()
    formatter.format("%d.%d.%d.%d",
        ipAddress and 0xFF,
        ipAddress shr 8 and 0xFF,
        ipAddress shr 16 and 0xFF,
        ipAddress shr 24 and 0xFF
    )
    return formatter.toString()
}