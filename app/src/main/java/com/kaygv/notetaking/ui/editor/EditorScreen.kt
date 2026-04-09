package com.kaygv.notetaking.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.kaygv.notetaking.domain.reminder.ReminderConstants
import com.kaygv.notetaking.ui.components.BlockTextField
import com.kaygv.notetaking.ui.components.EditorToolbar
import com.kaygv.notetaking.ui.components.OverflowMenu
import com.kaygv.notetaking.ui.components.OverflowMenuItem
import com.kaygv.notetaking.ui.components.TopBar
import com.kaygv.notetaking.ui.editor.markdown.EditorBlock
import com.kaygv.notetaking.ui.editor.markdown.MarkdownTransformation
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteDialogHost
import kotlinx.coroutines.android.awaitFrame

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    navController: NavController,
    noteId: Long,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isExist = state.noteId != null
    val isReminderExist = state.reminderTime != ReminderConstants.NO_REMINDER
    val isKeyboardVisible = WindowInsets.isImeVisible

    val listState = rememberLazyListState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.processIntent(EditorIntent.InsertImage(it.toString()))
        }
    }

    val topbarMenuList = mutableListOf(
        OverflowMenuItem(
            title = "Add to Folder",
            onClick = { viewModel.processIntent(EditorIntent.OpenFolderPicker) }
        ),
        OverflowMenuItem(
            title = if (isReminderExist) "Remove Reminder" else "Set Reminder",
            onClick = {
                if (isReminderExist) {
                    viewModel.processIntent(EditorIntent.RemoveReminder)
                } else {
                    viewModel.processIntent(EditorIntent.OpenSetReminderPicker)
                }
            }
        ),
    )

    if (isExist) {
        topbarMenuList.add(
            OverflowMenuItem(
                title = "Delete Note",
                onClick = {
                    state.noteId?.let {
                        viewModel.processIntent(EditorIntent.DeleteNote(it))
                    }
                }
            )
        )
    }

    LaunchedEffect(noteId) {
        if (noteId != -1L) {
            viewModel.processIntent(EditorIntent.LoadNote(noteId))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                EditorEvent.NoteSaved -> navController.popBackStack()
                EditorEvent.NoteDeleted -> navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        awaitFrame()
        // Focus is handled inside BlockTextField via focusedBlockId
    }

    LaunchedEffect(state.currentBlockId) {
        val index = state.blocks.indexOfFirst { it.id == state.currentBlockId }
        if (index != -1) {
            listState.animateScrollToItem(index)
        }
    }

    BackHandler(enabled = !state.isSaving) {
        viewModel.processIntent(EditorIntent.SaveNote)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Editor",
                leadingIcon = {
                    IconButton(onClick = { viewModel.processIntent(EditorIntent.SaveNote) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                trailingIcon = {
                    OverflowMenu(items = topbarMenuList)
                }
            )
        },
        bottomBar = {
            if (isKeyboardVisible) {
                EditorToolbar(
                    onBold = { viewModel.processIntent(EditorIntent.FormatBold) },
                    onItalic = { viewModel.processIntent(EditorIntent.FormatItalic) },
                    onUnderline = { viewModel.processIntent(EditorIntent.FormatUnderline) },
                    onCheckbox = { viewModel.processIntent(EditorIntent.InsertCheckbox) },
                    onBullet = { viewModel.processIntent(EditorIntent.InsertBullet) },
                    onNumbered = { viewModel.processIntent(EditorIntent.InsertNumbered) },
                    onInsertImage = { imagePickerLauncher.launch("image/*") },
                    onInsertLink = { viewModel.processIntent(EditorIntent.ToggleLinkDialog) },
                    onIndent = {
                        state.currentBlockId?.let {
                            viewModel.processIntent(EditorIntent.Indent(it)) } },
                    onOutdent = {
                        state.currentBlockId?.let {
                            viewModel.processIntent(EditorIntent.Outdent(it)) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->

        NoteDialogHost(
            dialog = state.dialog,
            onDismiss = { viewModel.processIntent(EditorIntent.DismissDialog) },
            onAction = { viewModel.handleAction(it) }
        )

        if (state.isLinkDialogOpen) {
            var text by remember { mutableStateOf("") }
            var url by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { viewModel.processIntent(EditorIntent.ToggleLinkDialog) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.processIntent(EditorIntent.InsertLink(text, url))
                        viewModel.processIntent(EditorIntent.ToggleLinkDialog)
                    }) { Text("Insert") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.processIntent(EditorIntent.ToggleLinkDialog) }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Insert Link") },
                text = {
                    Column {
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = { Text("Text") }
                        )
                        TextField(
                            value = url,
                            onValueChange = { url = it },
                            placeholder = { Text("URL") }
                        )
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        // FIX 2: Remove default item spacing
                        state = listState,
                        contentPadding = PaddingValues(bottom = 120.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.blocks, key = { it.id }) { block ->
                            when (block) {

                                is EditorBlock.Paragraph -> {
                                    BlockTextField(
                                        blockId = block.id,
                                        value = block.value,
                                        focusedBlockId = state.currentBlockId,
                                        visualTransformation = MarkdownTransformation(),
                                        onEnterPressed = { viewModel.splitBlock(block.id) },
                                        onValueChange = { viewModel.updateBlock(block.id, it) },
                                        onKeyEvent = { event, value ->
                                            if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace) {
                                                if (value.selection.min == 0) {
                                                    viewModel.mergeWithPrevious(block.id)
                                                    true
                                                } else false
                                            } else false
                                        }
                                    )
                                }

                                is EditorBlock.Heading -> {
                                    BlockTextField(
                                        blockId = block.id,
                                        value = block.value,
                                        focusedBlockId = state.currentBlockId,
                                        textStyle = MaterialTheme.typography.headlineMedium,
                                        visualTransformation = MarkdownTransformation(),
                                        singleLine = true,
                                        onDone = { viewModel.splitBlock(block.id) },
                                        onEnterPressed = { viewModel.splitBlock(block.id) },
                                        onValueChange = { viewModel.updateBlock(block.id, it) },
                                        onKeyEvent = { event, value ->
                                            if (event.type == KeyEventType.KeyDown && event.key == Key.Backspace) {
                                                if (value.selection.min == 0) {
                                                    viewModel.mergeWithPrevious(block.id)
                                                    true
                                                } else false
                                            } else false
                                        }
                                    )
                                }

                                is EditorBlock.Checkbox -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(start = (block.indent * 16).dp)
                                    ) {
                                        CompositionLocalProvider(
                                            LocalMinimumInteractiveComponentSize provides Dp.Unspecified
                                        ) {
                                            Checkbox(
                                                checked = block.checked,
                                                onCheckedChange = { viewModel.toggleCheckbox(block.id) }
                                            )
                                        }
                                        BlockTextField(
                                            blockId = block.id,
                                            value = block.value,
                                            focusedBlockId = state.currentBlockId,
                                            visualTransformation = MarkdownTransformation(),
                                            onEnterPressed = { viewModel.splitBlock(block.id) },
                                            onValueChange = { viewModel.updateBlock(block.id, it) },
                                            onKeyEvent = { event, value ->
                                                if (event.type == KeyEventType.KeyDown) {
                                                    when (event.key) {
                                                        Key.Backspace -> {
                                                            if (value.selection.min == 0) {
                                                                viewModel.mergeWithPrevious(block.id)
                                                                true
                                                            } else false
                                                        }

                                                        Key.Tab -> {
                                                            if (event.isShiftPressed) {
                                                                viewModel.processIntent(EditorIntent.Outdent(block.id))
                                                            } else {
                                                                viewModel.processIntent(EditorIntent.Indent(block.id))
                                                            }
                                                            true
                                                        }

                                                        else -> false
                                                    }
                                                } else false
                                            }
                                        )
                                    }
                                }

                                is EditorBlock.Bullet -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(start = (block.indent * 16).dp)
                                    ) {
                                        Text("• ")
                                        BlockTextField(
                                            blockId = block.id,
                                            value = block.value,
                                            focusedBlockId = state.currentBlockId,
                                            visualTransformation = MarkdownTransformation(),
                                            onEnterPressed = { viewModel.splitBlock(block.id) },
                                            onValueChange = { viewModel.updateBlock(block.id, it) },
                                            onKeyEvent = { event, value ->
                                                if (event.type == KeyEventType.KeyDown) {
                                                    when (event.key) {
                                                        Key.Backspace -> {
                                                            if (value.selection.min == 0) {
                                                                viewModel.mergeWithPrevious(block.id)
                                                                true
                                                            } else false
                                                        }

                                                        Key.Tab -> {
                                                            if (event.isShiftPressed) {
                                                                viewModel.processIntent(EditorIntent.Outdent(block.id))
                                                            } else {
                                                                viewModel.processIntent(EditorIntent.Indent(block.id))
                                                            }
                                                            true
                                                        }

                                                        else -> false
                                                    }
                                                } else false
                                            }
                                        )
                                    }
                                }

                                is EditorBlock.Numbered -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(start = (block.indent * 16).dp)
                                    ) {
                                        Text("${block.number}. ")
                                        BlockTextField(
                                            blockId = block.id,
                                            value = block.value,
                                            focusedBlockId = state.currentBlockId,
                                            visualTransformation = MarkdownTransformation(),
                                            onEnterPressed = { viewModel.splitBlock(block.id) },
                                            onValueChange = { viewModel.updateBlock(block.id, it) },
                                            onKeyEvent = { event, value ->
                                                if (event.type == KeyEventType.KeyDown) {
                                                    when (event.key) {
                                                        Key.Backspace -> {
                                                            if (value.selection.min == 0) {
                                                                viewModel.mergeWithPrevious(block.id)
                                                                true
                                                            } else false
                                                        }

                                                        Key.Tab -> {
                                                            if (event.isShiftPressed) {
                                                                viewModel.processIntent(EditorIntent.Outdent(block.id))
                                                            } else {
                                                                viewModel.processIntent(EditorIntent.Indent(block.id))
                                                            }
                                                            true
                                                        }

                                                        else -> false
                                                    }
                                                } else false
                                            }
                                        )
                                    }
                                }

                                is EditorBlock.Image -> {
                                    AsyncImage(
                                        model = block.url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}