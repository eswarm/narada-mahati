package `in`.eswarm.mahati.util

import android.content.Context
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.mahati.MahatiApplication

fun Context.getAppComponent(): AppComponent {
    val application = applicationContext as MahatiApplication
    return application.appComponent
}

