package `in`.eswarm.mahati.log

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.AppComponent
import `in`.eswarm.shared.LogLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(appComponent: AppComponent) {
    val viewModel: LogViewModel = viewModel(factory = LogViewModel.Factory(appComponent.logStream))
    val logs by viewModel.logs.collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Logs") }, actions = {
                IconButton(
                    onClick = { viewModel.clearLogs() },
                    content = { Icon(Icons.Filled.Delete, contentDescription = "Delete") })
            })
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            items(logs) { log ->
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
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return format.format(date)
}
