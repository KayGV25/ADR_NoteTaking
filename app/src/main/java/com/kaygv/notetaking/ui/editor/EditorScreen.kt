package com.kaygv.notetaking.ui.editor

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaygv.notetaking.ui.components.FolderPickerBottomSheet
import com.kaygv.notetaking.ui.components.OverflowMenu
import com.kaygv.notetaking.ui.components.OverflowMenuItem
import com.kaygv.notetaking.ui.components.TopBar
import com.kaygv.notetaking.ui.editor.markdown.MarkdownTransformation
import kotlinx.coroutines.android.awaitFrame

@Composable
fun EditorScreen(
    navController: NavController,
    noteId: Long,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val contentFocusRequester = remember { FocusRequester() }
    val isExist = state.noteId != null
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }


    val topbarMenuList = mutableListOf(
        OverflowMenuItem(
            title = "Add to Folder",
            onClick = {
                viewModel.processIntent(EditorIntent.OpenFolderPicker)
            }
        ),

        OverflowMenuItem(
            title = "Set Reminder",
            onClick = {
                // TODO open reminder picker
            }
        ),
    )
    if (isExist)
        topbarMenuList.add(
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
        awaitFrame()
        contentFocusRequester.requestFocus()
    }

    BackHandler(enabled = !state.isSaving) {
        viewModel.processIntent(EditorIntent.SaveNote)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Editor",
                leadingIcon = {
                    IconButton(
                        onClick = { viewModel.processIntent(EditorIntent.SaveNote) }
                    ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                } },
                trailingIcon = {
                    OverflowMenu(
                        items = topbarMenuList
                    )
                }
            )},
        modifier = Modifier
            .fillMaxSize()
    ) { padding ->
        if (state.isFolderPickerVisible) {
            FolderPickerBottomSheet(
                folders = state.folders,
                newFolderName = state.newFolderName,
                isCreating = state.isCreatingFolder,

                onStartCreate = {
                    viewModel.processIntent(EditorIntent.StartCreateFolder)
                },
                onUpdateFolderName = {
                    viewModel.processIntent(EditorIntent.UpdateNewFolderName(it))
                },
                onCreate = {
                    viewModel.processIntent(EditorIntent.CreateFolder)
                },

                onSelect = {
                    viewModel.processIntent(EditorIntent.AssignToFolder(it))
                },
                onDismiss = {
                    viewModel.processIntent(
                        EditorIntent.AssignToFolder(state.folderId)
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp)
        ) {
            Box {
                BasicTextField(
                    value = state.content,
                    onValueChange = {
                        viewModel.processIntent(EditorIntent.UpdateContent(it))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()

                                    if (event.changes.any { it.pressed }) {

                                        val change = event.changes.firstOrNull() ?: continue
                                        val offset = change.position

                                        val layout = layoutResult ?: continue

                                        val position = layout.getOffsetForPosition(offset)
                                        val lineIndex = layout.getLineForOffset(position)

                                        val lines = state.content.text.lines()
                                        if (lineIndex >= lines.size) continue

                                        val line = lines[lineIndex]

                                        if (!line.startsWith("- [ ] ") && !line.startsWith("- [x] ")) continue

                                        val lineStart = layout.getLineStart(lineIndex)

                                        val checkboxLength = 6
                                        val checkboxEnd = (lineStart + checkboxLength).coerceAtMost(layout.layoutInput.text.length)

                                        val startBox = layout.getBoundingBox(lineStart)
                                        val endBox = layout.getBoundingBox((checkboxEnd - 1).coerceAtLeast(lineStart))

                                        val isInCheckboxArea =
                                            offset.x in startBox.left..endBox.right &&
                                                    offset.y in startBox.top..startBox.bottom

                                        if (isInCheckboxArea) {
                                            when {
                                                line.startsWith("- [ ] ") -> {
                                                    viewModel.processIntent(
                                                        EditorIntent.ToggleCheckbox(lineIndex, true)
                                                    )
                                                }

                                                line.startsWith("- [x] ") -> {
                                                    viewModel.processIntent(
                                                        EditorIntent.ToggleCheckbox(lineIndex, false)
                                                    )
                                                }
                                            }

                                            change.consume() // ONLY consume when checkbox tapped
                                        }
                                    }
                                }
                            }
                        },
                    onTextLayout = {
                        layoutResult = it
                    },
                    textStyle = LocalTextStyle.current,
                    cursorBrush = SolidColor(Color.Black),
                    visualTransformation = MarkdownTransformation()
                )

            }

        }
    }

}