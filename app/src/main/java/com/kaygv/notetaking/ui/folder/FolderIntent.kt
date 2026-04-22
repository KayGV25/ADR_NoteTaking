package com.kaygv.notetaking.ui.folder

import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.domain.model.Note
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteAction
import com.kaygv.notetaking.ui.mvi.MviIntent

sealed class FolderIntent : MviIntent {
    data class UpdateName(
        val name: String
    ) : FolderIntent()
    data class DeleteFolder(val folder: Folder) : FolderIntent()
    data class SearchFolders(val query: String) : FolderIntent()
    data class OnLongPressFolder(val folder: Folder) : FolderIntent()
    data class OpenRenameDialog(val folder: Folder): FolderIntent()
    data class OpenDeleteDialog(val folder: Folder): FolderIntent()
    data object ConfirmRename: FolderIntent()
    data object ConfirmDelete: FolderIntent()
    data object DismissDialog: FolderIntent()
    data class OpenNoteMenu(val note: Note): FolderIntent()
    data object DismissNoteDialog: FolderIntent()
    data class NoteActionIntent(val action: NoteAction): FolderIntent()
    data object DismissFolderBottomSheet: FolderIntent()
    data class OpenFolderBottomSheet(val selectedFolder: FolderWithNotes): FolderIntent()
    data object OpenCreateDialog : FolderIntent()
    data object ConfirmCreate : FolderIntent()
}