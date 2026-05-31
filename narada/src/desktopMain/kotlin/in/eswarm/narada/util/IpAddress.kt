package `in`.eswarm.narada.util

/**
 * Dummy implementation for desktop. Returns null as QR code sharing is not supported.
 */
actual fun getLocalIpAddress(): String? {
    return null
}