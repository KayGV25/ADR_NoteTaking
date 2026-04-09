package com.kaygv.notetaking.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaygv.notetaking.domain.reminder.ReminderConstants
import com.kaygv.notetaking.ui.components.NoteCard
import com.kaygv.notetaking.ui.components.NotePreview
import com.kaygv.notetaking.ui.components.NotePreviewButtonConfig
import com.kaygv.notetaking.ui.components.NotePreviewConfig
import com.kaygv.notetaking.ui.components.SearchBar
import com.kaygv.notetaking.ui.editor.markdown.MarkdownTransformation
import com.kaygv.notetaking.ui.navigation.Routes
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteDialogHost
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
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            ) {
                state.groupedNotes.forEach { (header, notes) ->
                    item {
                        Text(
                            text = header,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note,
                            onClick = {
                                navController.navigate("${Routes.EDITOR}?noteId=${note.id}")
                            },
                            onLongClick = {
                                viewModel.processIntent(
                                    HomeIntent.OpenNoteMenu(note)
                                )
                            }
                        )
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