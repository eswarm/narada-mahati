package `in`.eswarm.narada.home

import `in`.eswarm.narada.AppComponent

class LaunchViewModelFactory {
    fun create(): HomeViewModel {
        return HomeViewModel(AppComponent.INSTANCE.logStream)
    }
}