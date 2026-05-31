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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import `in`.eswarm.shared.LogData
import `in`.eswarm.shared.LogLevel
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(Dp(8f))
    ) {
        items(logState.value) { log ->
            val color = when (log.level) {
                LogLevel.INFO -> Color.Yellow
                LogLevel.DEBUG -> Color.Gray
                LogLevel.WARN -> Color(0xFFFFA500) // Orange
                LogLevel.ERROR -> Color.Red
                LogLevel.VERBOSE -> Color.White
            }
            Text(
                text = "${formatTimestamp(log.timestamp)} [${log.tag}] ${log.msg}\n",
                color = color
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return format.format(date)
}
