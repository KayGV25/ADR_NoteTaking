package com.kaygv.notetaking.ui.dialog.noteDialog

sealed class NoteDialog {
    data object None : NoteDialog()
    data class Reminder(val noteId: Long, val currentTime: Long?) : NoteDialog()
    data class Folder(
        val folders: List<com.kaygv.notetaking.domain.model.Folder>,
        val noteId: Long,
        val selectedFolderId: Long? = null
    ) : NoteDialog()
}