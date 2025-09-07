package `in`.eswarm.mahati

import android.app.Application

class MahatiApplication : Application() {

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