package `in`.eswarm.narada.launch

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp


@Composable
fun LogView(logs: List<String>) {

    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        listState.animateScrollToItem(index = if (logs.isNotEmpty()) logs.size - 1 else logs.size)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(Dp(8f))
    ) {
        items(logs) { log ->
            Text(log)
        }
    }
}