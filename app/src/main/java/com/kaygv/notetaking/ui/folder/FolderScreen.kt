package com.kaygv.notetaking.ui.folder

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaygv.notetaking.ui.components.BottomBar
import com.kaygv.notetaking.ui.components.SearchBar
import com.kaygv.notetaking.ui.home.HomeIntent

@Composable
fun FolderScreen(
    navController: NavController,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                    })

                OutlinedTextField(
                    value = state.newFolderName,
                    onValueChange = {
                        viewModel.processIntent(
                            FolderIntent.UpdateName(it)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Folder name") }
                )

            }

            LazyColumn {

                items(state.folders) { folder ->
                    ListItem(
                        headlineContent = {
                            Text(folder.name)
                        },
                        modifier = Modifier
                            .clickable {
                                navController.navigate("folder/${folder.id}")
                            }
                    )

                }

            }
        }
    }
}