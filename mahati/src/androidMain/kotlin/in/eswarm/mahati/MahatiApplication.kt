package `in`.eswarm.mahati

import android.app.Application
import `in`.eswarm.mahati.db.DriverFactory
import `in`.eswarm.mahati.db.initializeDb
import `in`.eswarm.mahati.mqtt.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MahatiApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initializeDb(DriverFactory(this))
        AppContext.init(this)
        appComponent = AppComponent()

        CoroutineScope(Dispatchers.IO).launch {
            val connections = appComponent.connectionRepo.getAllConnections()
            for (connection in connections) {
                appComponent.mqttController.addConnection(connection)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        appComponent.clear()
    }
}
