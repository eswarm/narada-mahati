package `in`.eswarm.mahati.mqtt.di

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AppContext {
    lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
