package com.kaygv.notetaking.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.domain.reminder.ReminderConstants
import com.kaygv.notetaking.ui.mvi.MviState

data class EditorState(
    val noteId: Long? = null,
    val title: String = "",
    val content: TextFieldValue = TextFieldValue(
        text = "# ",
        selection = TextRange(2)
    ),
    val reminderTime: Long = ReminderConstants.NO_REMINDER,
    val folderId: Long? = null,
    val isSaving: Boolean = false,
    val createdAt: Long? = null,

    val isFolderPickerVisible: Boolean = false,
    val folders: List<Folder> = emptyList(),

    val newFolderName: String = "",
    val isCreatingFolder: Boolean = false

) : MviState
