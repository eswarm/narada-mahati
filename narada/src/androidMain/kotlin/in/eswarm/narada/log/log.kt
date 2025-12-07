package `in`.eswarm.narada.log

import android.util.Log

internal actual fun log(tag: String, message: String) {
    Log.i(tag, message)
}
