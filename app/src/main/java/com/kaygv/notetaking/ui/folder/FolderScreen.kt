package com.kaygv.notetaking.ui.folder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaygv.notetaking.ui.components.FolderItem
import com.kaygv.notetaking.ui.components.SearchBar
import com.kaygv.notetaking.ui.components.bottomsheet.FolderBottomSheet
import com.kaygv.notetaking.ui.dialog.folderDialog.FolderDialog
import com.kaygv.notetaking.ui.navigation.Routes

@Composable
fun FolderScreen(
    navController: NavController,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val folders by viewModel.folder.collectAsState()

    if (state.dialog is FolderDialog.Rename) {
        AlertDialog(
            onDismissRequest = {
                viewModel.processIntent(FolderIntent.DismissDialog)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.processIntent(FolderIntent.ConfirmRename)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.processIntent(FolderIntent.DismissDialog)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Rename Folder") },
            text = {
                TextField(
                    value = state.newFolderName,
                    onValueChange = {
                        viewModel.processIntent(FolderIntent.UpdateName(it))
                    }
                )
            }
        )
    }

    if (state.dialog is FolderDialog.Delete) {
        AlertDialog(
            onDismissRequest = {
                viewModel.processIntent(FolderIntent.DismissDialog)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.processIntent(FolderIntent.ConfirmDelete)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.processIntent(FolderIntent.DismissDialog)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Folder") },
            text = { Text("All notes will be unassigned from this folder.") }
        )
    }

    if (state.dialog is FolderDialog.Menu) {
        val folder = (state.dialog as FolderDialog.Menu).folder

        AlertDialog(
            onDismissRequest = {
                viewModel.processIntent(FolderIntent.DismissDialog)
            },
            confirmButton = {},
            title = { Text(folder.name) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                        viewModel.processIntent(FolderIntent.OpenRenameDialog(folder))
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename Folder")
                            Text("Rename Folder", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    TextButton(
                        onClick = {
                            viewModel.processIntent(FolderIntent.OpenDeleteDialog(folder))
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Folder")
                            Text("Delete Folder", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        )
    }

    if (state.dialog is FolderDialog.Create) {
        AlertDialog(
            onDismissRequest = {
                viewModel.processIntent(FolderIntent.DismissDialog)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.processIntent(FolderIntent.ConfirmCreate)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.processIntent(FolderIntent.DismissDialog)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Create Folder") },
            text = {
                TextField(
                    value = state.newFolderName,
                    onValueChange = {
                        viewModel.processIntent(FolderIntent.UpdateName(it))
                    },
                    placeholder = { Text("Folder name") }
                )
            }
        )
    }

    if (state.selectedFolder != null) {
        FolderBottomSheet(
            folder = state.selectedFolder!!,
            onDismiss = {
                viewModel.processIntent(FolderIntent.DismissFolderBottomSheet)
            },
            onNoteClick = {
                navController.navigate("${Routes.EDITOR}?noteId=$it")
            },
            onNoteLongPress = {
                viewModel.processIntent(FolderIntent.OpenNoteMenu(it))
            },
            dialog = state.noteDialog,
            onDialogDismiss = {
                viewModel.processIntent(FolderIntent.DismissNoteDialog)
            },
            onDialogAction = { viewModel.processIntent(FolderIntent.NoteActionIntent(it))}
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.processIntent(
                        FolderIntent.OpenCreateDialog
                    )
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            ) {
                items(folders) { folder ->
                    FolderItem(
                        folderWithNotes = folder,
                        isOpen = state.selectedFolder?.folder?.id == folder.folder.id,
                        onClick = { viewModel.processIntent(FolderIntent.OpenFolderBottomSheet(it)) },
                        onLongPress = { viewModel.processIntent(FolderIntent.OnLongPressFolder(it.folder)) }
                    )
                }

            }
        }
    }
}