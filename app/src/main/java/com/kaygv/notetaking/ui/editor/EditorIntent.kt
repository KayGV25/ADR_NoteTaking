package com.kaygv.notetaking.ui.editor

import androidx.compose.ui.text.input.TextFieldValue
import com.kaygv.notetaking.ui.mvi.MviIntent

sealed class EditorIntent : MviIntent {
    data class LoadNote(
        val noteId: Long
    ) : EditorIntent()

    data class UpdateContent(
        val content: TextFieldValue
    ) : EditorIntent()

    data class SetReminder(
        val reminderTime: Long
    ) : EditorIntent()

    object RemoveReminder : EditorIntent()

    data object SaveNote : EditorIntent()

    data class DeleteNote(
        val noteId: Long
    ) : EditorIntent()

    data object OpenFolderPicker : EditorIntent()

    data class AssignToFolder(
        val folderId: Long?
    ) : EditorIntent()

    data object StartCreateFolder : EditorIntent()

    data class UpdateNewFolderName(val name: String) : EditorIntent()

    data object CreateFolder : EditorIntent()

    data class ToggleCheckbox(
        val lineIndex: Int,
        val checked: Boolean
    ) : EditorIntent()

    data object CloseSetReminderPicker : EditorIntent()
    data object OpenSetReminderPicker : EditorIntent()

    object DismissDialog : EditorIntent()
}