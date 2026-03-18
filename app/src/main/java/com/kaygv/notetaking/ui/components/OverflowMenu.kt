package com.kaygv.notetaking.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

data class OverflowMenuItem(
    val title: String,
    val onClick: () -> Unit
)

@Composable
fun OverflowMenu(
    items: List<OverflowMenuItem>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {

        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            items.forEach { item ->

                DropdownMenuItem(
                    text = { Text(item.title) },
                    onClick = {
                        expanded = false
                        item.onClick()
                    }
                )

            }
        }
    }
}