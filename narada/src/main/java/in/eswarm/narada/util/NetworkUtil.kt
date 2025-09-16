package `in`.eswarm.narada.util

import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

object NetworkUtil {

    fun getLocalIpAddress(): String? {
        try {
            val nwInterfaceList: Enumeration<NetworkInterface> =
                NetworkInterface.getNetworkInterfaces()
            for (nwInterface in nwInterfaceList) {
                val inetAddressList = nwInterface.inetAddresses
                for (inetAddress in inetAddressList) {
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            Log.e("NetworkUtil", ex.message ?: "")
        }
        return null
    }
}