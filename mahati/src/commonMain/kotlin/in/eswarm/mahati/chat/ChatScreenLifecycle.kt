package `in`.eswarm.mahati.chat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ChatScreenLifecycle {
    private val _isChatScreenVisible = MutableStateFlow(false)
    val isChatScreenVisible = _isChatScreenVisible.asStateFlow()

    fun onChatScreenVisible() {
        _isChatScreenVisible.value = true
    }

    fun onChatScreenHidden() {
        _isChatScreenVisible.value = false
    }
}
