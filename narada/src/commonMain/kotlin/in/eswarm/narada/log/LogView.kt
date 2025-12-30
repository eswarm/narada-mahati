package `in`.eswarm.narada.log

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import `in`.eswarm.shared.LogData
import kotlinx.coroutines.flow.Flow


@Composable
fun LogView(logs: Flow<List<LogData>>) {

    val listState = rememberLazyListState()
    val logState = logs.collectAsState(initial = emptyList())

    LaunchedEffect(logState.value.size) {
        if (logState.value.isNotEmpty()) {
            listState.animateScrollToItem(index = logState.value.size - 1)
        }
    }

    LazyColumn(
        state = listState, modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(Dp(8f))
    ) {
        items(logState.value) { log ->
            Text("${log.timestamp} ${log.tag} ${log.msg}")
        }
    }
}