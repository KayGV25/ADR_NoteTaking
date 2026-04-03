package com.kaygv.notetaking.ui.folder

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColor
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaygv.notetaking.ui.components.BottomBar
import com.kaygv.notetaking.ui.components.FolderAccordionItem
import com.kaygv.notetaking.ui.components.SearchBar
import com.kaygv.notetaking.ui.home.HomeIntent
import com.kaygv.notetaking.ui.navigation.Routes

@Composable
fun FolderScreen(
    navController: NavController,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val folders by viewModel.folder.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.processIntent(
                        FolderIntent.CreateFolder
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Folder"
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Row(
                modifier = Modifier
            ) {
                SearchBar(
                    query = state.searchQuery,
                    placeholder = "Search folders...",
                    onQueryChange = {
                        viewModel.processIntent(
                            FolderIntent.SearchFolders(it)
                        )
                    }
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            ) {
                items(folders) { folder ->
                    FolderAccordionItem(
                        folderWithNotes = folder,
                    ) {
                        navController.navigate("${Routes.EDITOR}?noteId=$it")
                    }

                }

            }
        }
    }
}