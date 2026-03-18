package com.kaygv.notetaking.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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

        }

    ) { padding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding)
        ) { page ->

            when (page) {

                0 -> HomeScreen(navController)

                1 -> FolderScreen(navController)

            }

        }

    }
}