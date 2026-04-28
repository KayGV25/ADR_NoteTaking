package com.vn.kaygv.notetaking.ui.dialog.noteDialog

sealed class NoteDialog {
    data object None : NoteDialog()
    data class Reminder(
        val noteId: Long,
        val noteTitle: String,
        val noteContent: String,
        val currentTime: Long?) : NoteDialog()
    data class Folder(
        val folders: List<com.vn.kaygv.notetaking.domain.model.Folder>,
        val noteId: Long,
        val selectedFolderId: Long? = null
    ) : NoteDialog()
}