package com.vn.kaygv.notetaking.ui.home

import com.vn.kaygv.notetaking.domain.model.Note
import com.vn.kaygv.notetaking.ui.mvi.MviIntent

sealed class HomeIntent : MviIntent {
    data object LoadNotes : HomeIntent()

    data class SearchNotes(val query: String) : HomeIntent()

    data class DeleteNote(val noteId: Long) : HomeIntent()
    data class SetNoteReminder(val noteId: Long?, val reminderTime: Long) : HomeIntent()
    data class RemoveReminder(val noteId: Long) : HomeIntent()
    data class AssignToFolder(val noteId: Long, val folderId: Long?) : HomeIntent()
    data object OpenFolderPicker : HomeIntent()
    data object OpenSetReminderPicker : HomeIntent()
    data class OpenNoteMenu(val note: Note) : HomeIntent()
    data object CloseNoteMenu : HomeIntent()
    data object StartCreateFolder : HomeIntent()
    data class UpdateNewFolderName(val name: String) : HomeIntent()
    data object CreateFolder : HomeIntent()
    data object CloseFolderPicker : HomeIntent()
    data object CloseSetReminderPicker : HomeIntent()

    data object DismissDialog : HomeIntent()
}