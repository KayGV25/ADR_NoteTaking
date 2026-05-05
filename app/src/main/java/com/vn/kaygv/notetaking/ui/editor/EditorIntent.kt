package com.vn.kaygv.notetaking.ui.editor

import com.vn.kaygv.notetaking.ui.mvi.MviIntent

sealed class EditorIntent : MviIntent {
    data class LoadNote(
        val noteId: Long
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
        val id: String
    ) : EditorIntent()

    data object CloseSetReminderPicker : EditorIntent()
    data object OpenSetReminderPicker : EditorIntent()

    data object DismissDialog : EditorIntent()

    data object FormatBold : EditorIntent()
    data object FormatItalic : EditorIntent()
    data object FormatUnderline : EditorIntent()
    data object InsertCheckbox : EditorIntent()
    data object InsertBullet : EditorIntent()
    data object InsertNumbered : EditorIntent()
    data object ToggleImagePicker : EditorIntent()
    data object ToggleLinkDialog : EditorIntent()

    data class InsertImage(val uri: String) : EditorIntent()
    data class InsertLink(val text: String, val url: String) : EditorIntent()

    data class Indent(val id: String) : EditorIntent()
    data class Outdent(val id: String) : EditorIntent()

}