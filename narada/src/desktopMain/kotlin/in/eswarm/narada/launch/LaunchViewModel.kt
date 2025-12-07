package `in`.eswarm.narada.launch

import `in`.eswarm.narada.AppComponent
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher

actual class LaunchViewModelFactory {
    actual fun create(): LaunchViewModel {
        return LaunchViewModel(AppComponent.INSTANCE.logStream)
    }
}