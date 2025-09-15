package `in`.eswarm.mahati

import android.app.Application
import `in`.eswarm.mahati.db.DriverFactory
import `in`.eswarm.mahati.db.initializeDb

class MahatiApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initializeDb(DriverFactory(this))
        appComponent = AppComponent()
    }

    override fun onTerminate() {
        super.onTerminate()
        appComponent.clear()
    }
}
