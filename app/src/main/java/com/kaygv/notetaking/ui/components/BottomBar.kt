package com.kaygv.notetaking.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kaygv.notetaking.R
import com.kaygv.notetaking.ui.navigation.Routes

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun BottomBar(
    currentPage: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {

        NavigationBarItem(
            selected = currentPage == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                Icons.Default.Home,
                    contentDescription = "Home")
                   },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentPage == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    ImageVector.vectorResource(id = R.drawable.folder_24px),
                    contentDescription = "Folders")
                   },
            label = { Text("Folders") }
        )

    }
}