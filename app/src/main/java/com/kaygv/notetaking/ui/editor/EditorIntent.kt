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
}