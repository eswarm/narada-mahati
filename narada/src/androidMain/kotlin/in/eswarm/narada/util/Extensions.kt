package `in`.eswarm.narada.util

import android.content.Context
import `in`.eswarm.narada.AppComponent
import `in`.eswarm.narada.NaradaApplication

fun Context.getAppComponent(): AppComponent {
    val application = applicationContext as NaradaApplication
    return application.appComponent
}