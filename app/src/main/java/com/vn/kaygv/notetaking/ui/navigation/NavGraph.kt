package com.vn.kaygv.notetaking.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vn.kaygv.notetaking.ui.MainScreen
import com.vn.kaygv.notetaking.ui.editor.EditorScreen

@Composable
fun AppNavGraph(
    openNoteId: Long
) {
    val navController = rememberNavController()

    LaunchedEffect(openNoteId) {
        if (openNoteId != -1L) {
            navController.navigate("${Routes.EDITOR}?noteId=$openNoteId")
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(500),
                initialOffsetX = { it } // slide from right
            )
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(500),
                targetOffsetX = { -it } // slide to left
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(500),
                initialOffsetX = { -it } // slide from left
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(500),
                targetOffsetX = { it } // slide to right
            )
        }
    ) {
        composable(Routes.MAIN) {
            MainScreen(navController)
        }

        composable(
            route = "${Routes.EDITOR}?noteId={noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L

            EditorScreen(
                navController = navController,
                noteId = noteId
            )
        }
    }
}