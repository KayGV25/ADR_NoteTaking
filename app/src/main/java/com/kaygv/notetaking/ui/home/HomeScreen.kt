package com.kaygv.notetaking.ui.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaygv.notetaking.domain.reminder.ReminderConstants
import com.kaygv.notetaking.ui.components.AdCard
import com.kaygv.notetaking.ui.components.NoteCard
import com.kaygv.notetaking.ui.components.NotePreview
import com.kaygv.notetaking.ui.components.NotePreviewButtonConfig
import com.kaygv.notetaking.ui.components.NotePreviewConfig
import com.kaygv.notetaking.ui.components.SearchBar
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteDialogHost
import com.kaygv.notetaking.ui.editor.markdown.MarkdownTransformation
import com.kaygv.notetaking.ui.navigation.Routes
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isMenuVisible && state.selectedNote != null) {
        val note = state.selectedNote!!
        val transformedContent = remember(note.content.text) {
            MarkdownTransformation().filter(AnnotatedString(note.content.text.take(500))).text
        }
        val hasReminder = state.reminderTime != null &&
                state.reminderTime != ReminderConstants.NO_REMINDER
        NotePreview(
            notePreviewConfig = NotePreviewConfig(
                content = transformedContent,
                onDismiss = {
                    viewModel.processIntent(HomeIntent.CloseNoteMenu)
                },
                onPreviewClick = {
                    navController.navigate("${Routes.EDITOR}?noteId=${note.id}")
                },
                buttons = listOf(
                    NotePreviewButtonConfig(
                        text = "Add to Folder",
                        icon = Icons.Default.Add,
                        onClick = { viewModel.processIntent(HomeIntent.OpenFolderPicker) }
                    ),
                    NotePreviewButtonConfig(
                        text = if (hasReminder) {
                            "Remove Reminder (${formatTime(state.reminderTime!!)})"
                        } else {
                            "Set Reminder"
                        },
                        icon = Icons.Default.Notifications,
                        onClick = {
                            if (hasReminder) {
                                viewModel.processIntent(
                                    HomeIntent.RemoveReminder(note.id)
                                )
                                viewModel.processIntent(
                                    HomeIntent.CloseNoteMenu
                                )
                            } else {
                                viewModel.processIntent(HomeIntent.OpenSetReminderPicker)
                            }
                        }
                    )
                ),
                onDelete = {
                    viewModel.processIntent(HomeIntent.DeleteNote(note.id))
                    viewModel.processIntent(HomeIntent.CloseNoteMenu)
                },
            )
        )
    }

    NoteDialogHost(
        dialog = state.dialog,
        onDismiss = {
            viewModel.processIntent(HomeIntent.DismissDialog)
        },
        onAction = {
            viewModel.handleAction(it)
        }
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("editor")
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Create Note"
                )
            }
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = state.searchQuery,
                placeholder = "Search notes...",
                onQueryChange = {
                    viewModel.processIntent(
                        HomeIntent.SearchNotes(it)
                    )
                },
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .weight(1f)
            ) {
                items(
                    items = state.feed,
                    span = { item ->
                        when (item) {
                            is UiItem.Header -> GridItemSpan(2)
                            else -> GridItemSpan(1)
                        }
                    }
                ) { item ->
                    when (item) {

                        is UiItem.Header -> {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        is UiItem.NoteItem -> {
                            NoteCard(
                                item.note,
                                onClick = {
                                    navController.navigate("${Routes.EDITOR}?noteId=${item.note.id}")
                                },
                                onLongClick = {
                                    viewModel.processIntent(HomeIntent.OpenNoteMenu(item.note))
                                }
                            )
                        }

                        is UiItem.AdItem -> {
                            AdCard(item.ad)
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(time: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(time))
}