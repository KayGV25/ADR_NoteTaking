package com.kaygv.notetaking.ui.noteDialog

sealed class NoteAction {
    data class SetReminder(val noteId: Long, val time: Long) : NoteAction()
    data class RemoveReminder(val noteId: Long) : NoteAction()
    data class AssignFolder(val noteId: Long, val folderId: Long?) : NoteAction()
    data class Delete(val noteId: Long) : NoteAction()
}