package com.kaygv.notetaking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaygv.notetaking.ui.FadeEdge
import com.kaygv.notetaking.ui.fadingEdge
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun WheelPicker(
    items: List<String>,
    startIndex: Int = 0,
    onSelected: (Int) -> Unit,
    modifier: Modifier
) {
    val loopCount = 500
    val totalItems = items.size * loopCount
    val start = totalItems / 2 + startIndex
    val itemHeight = 40.dp

    val listState = rememberLazyListState(start)


    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .minByOrNull { kotlin.math.abs(it.offset) }
                ?.index ?: 0
        }
            .distinctUntilChanged()
            .collect { index ->
                onSelected(index % items.size)
            }
    }


    Box(
        modifier = modifier
            .height(150.dp)
            .fadingEdge(FadeEdge.VERTICAL)
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = rememberSnapFlingBehavior(listState),
            contentPadding = PaddingValues(
                vertical = (150.dp - itemHeight) / 2
            )
        ) {
            items(totalItems) { index ->
                Text(
                    text = items[index % items.size],
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .height(40.dp)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
        )
    }
}
