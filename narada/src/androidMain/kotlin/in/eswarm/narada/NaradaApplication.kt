package `in`.eswarm.narada

import android.app.Application
import `in`.eswarm.narada.util.AppContext
import `in`.eswarm.narada.util.preferences

class NaradaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.context = this
        val appComponent = AppComponent(this.preferences)
        AppComponent.INSTANCE = appComponent
    }

    override fun onTerminate() {
        super.onTerminate()
        AppComponent.INSTANCE.clear()
    }
}