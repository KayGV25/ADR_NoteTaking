package com.kaygv.notetaking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaygv.notetaking.R

@Composable
fun BottomBar(
    currentPage: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.scrim
    ) {

        NavigationBarItem(
            selected = currentPage == 0,
            onClick = { onTabSelected(0) },
            icon = { HomeIcon(currentPage == 0) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.tertiary,
                selectedTextColor = MaterialTheme.colorScheme.tertiary,
                indicatorColor = Color.Transparent,
            ),
        )

        NavigationBarItem(
            selected = currentPage == 1,
            onClick = { onTabSelected(1) },
            icon = { FolderIcon(currentPage == 1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.tertiary,
                selectedTextColor = MaterialTheme.colorScheme.tertiary,
                indicatorColor = Color.Transparent,
            )
        )

    }
}

@Composable
private fun FolderIcon(isActive: Boolean) =
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(64.dp)
    ) {
        if (!isActive) {
            Icon(
                ImageVector.vectorResource(id = R.drawable.folder_24px),
                contentDescription = "Folders",
                modifier = Modifier.size(28.dp)
            )
        } else {
            Icon(
                ImageVector.vectorResource(id = R.drawable.folder_filled_24px),
                contentDescription = "Folders",
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "Folders",
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            style = if (isActive) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall
        )
    }


@Composable
private fun HomeIcon(isActive: Boolean) =
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(64.dp)
    ) {
        if (isActive) {
            Icon(
                Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier.size(28.dp)
            )
        } else {
            Icon(
                Icons.Outlined.Home,
                contentDescription = "Home",
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "Home",
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            style = if (isActive) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall
        )
    }