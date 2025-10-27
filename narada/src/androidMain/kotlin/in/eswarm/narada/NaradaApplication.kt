package `in`.eswarm.narada

import android.app.Application

class NaradaApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = AppComponent()
    }

    override fun onTerminate() {
        super.onTerminate()
        appComponent.clear()
    }

}