package com.vn.kaygv.notetaking.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.vn.kaygv.notetaking.domain.model.Folder
import com.vn.kaygv.notetaking.domain.reminder.ReminderConstants
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteDialog
import com.vn.kaygv.notetaking.ui.editor.markdown.EditorBlock
import com.vn.kaygv.notetaking.ui.mvi.MviState

data class EditorState(
    val noteId: Long? = null,
    val title: String = "",
    val blocks: List<EditorBlock> = listOf(
        EditorBlock.Heading(
            value = TextFieldValue("")
        )
    ),


    val currentBlockId: String? = blocks.first().id,
    val currentSelection: TextRange = TextRange.Zero,

    val reminderTime: Long = ReminderConstants.NO_REMINDER,
    val folderId: Long? = null,
    val isSaving: Boolean = false,
    val createdAt: Long? = null,

    val isFolderPickerVisible: Boolean = false,
    val folders: List<Folder> = emptyList(),

    val newFolderName: String = "",
    val isCreatingFolder: Boolean = false,

    val isSetReminderPickerVisible: Boolean = false,
    val dialog: NoteDialog = NoteDialog.None,
    val isImagePickerOpen: Boolean = false,
    val isLinkDialogOpen: Boolean = false,
) : MviState
