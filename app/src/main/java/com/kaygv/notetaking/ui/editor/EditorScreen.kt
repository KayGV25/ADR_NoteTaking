package com.kaygv.notetaking.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaygv.notetaking.ui.components.OverflowMenu
import com.kaygv.notetaking.ui.components.OverflowMenuItem
import com.kaygv.notetaking.ui.components.TopBar

@Composable
fun EditorScreen(
    navController: NavController,
    noteId: Long,
    viewModel: EditorViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()
    val contentFocusRequester = remember { FocusRequester() }


    LaunchedEffect(noteId) {

        if (noteId != -1L) {
            viewModel.processIntent(
                EditorIntent.LoadNote(noteId)
            )
        }

    }

    LaunchedEffect(viewModel) {

        viewModel.event.collect { event ->

            when(event) {

                EditorEvent.NoteSaved -> {
                    navController.popBackStack()
                }

                EditorEvent.NoteDeleted -> {
                    navController.popBackStack()
                }

            }

        }
    }

    LaunchedEffect(Unit) {
        contentFocusRequester.requestFocus()
    }

    BackHandler {
        viewModel.processIntent(EditorIntent.SaveNote)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Editor",
                leadingIcon = { IconButton( onClick = { viewModel.processIntent(EditorIntent.SaveNote) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                } },
                trailingIcon = {
                    OverflowMenu(
                        items = listOf(
                            OverflowMenuItem(
                                title = "Add to Folder",
                                onClick = {
                                    // TODO open folder picker
                                }
                            ),

                            OverflowMenuItem(
                                title = "Set Reminder",
                                onClick = {
                                    // TODO open reminder picker
                                }
                            ),

                            OverflowMenuItem(
                                title = "Delete Note",
                                onClick = {
                                    state.noteId?.let {
                                        viewModel.processIntent(
                                            EditorIntent.DeleteNote(it)
                                        )
                                    }
                                }
                            )
                        )
                    )
                }
            )},
        modifier = Modifier
            .fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(ScrollState(0), true)
        ) {
            TextField(
                value = state.content,
                onValueChange = {
                    viewModel.processIntent(
                        EditorIntent.UpdateContent(it)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .focusRequester(contentFocusRequester),
            )
        }
    }

}