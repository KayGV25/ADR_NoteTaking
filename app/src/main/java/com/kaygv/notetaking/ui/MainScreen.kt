package com.kaygv.notetaking.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import com.kaygv.notetaking.ui.components.BottomBar
import com.kaygv.notetaking.ui.folder.FolderScreen
import com.kaygv.notetaking.ui.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavController
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        bottomBar = {
            BottomBar(
                currentPage = pagerState.currentPage,
                onTabSelected = { page ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )
        },
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
    ) { padding ->

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> HomeScreen(navController)
                1 -> FolderScreen(navController)
            }
        }

    }
}