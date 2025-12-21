package `in`.eswarm.mahati.log

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.eswarm.mahati.AppComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(appComponent: AppComponent) {
    val viewModel: LogViewModel = viewModel(factory = LogViewModel.Factory(appComponent.logStream))
    val logs by viewModel.logs.collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Logs") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            reverseLayout = true
        ) {
            items(logs) { log ->
                Text(text = log.msg)
            }
        }
    }
}
