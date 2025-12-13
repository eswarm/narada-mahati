package `in`.eswarm.narada.util

import android.widget.Toast

actual fun showToast(message: String) {
    Toast.makeText(AppContext.context, message, Toast.LENGTH_LONG).show()
}
