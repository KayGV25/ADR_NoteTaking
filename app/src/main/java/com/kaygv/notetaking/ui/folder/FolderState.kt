package com.kaygv.notetaking.ui.folder

import com.kaygv.notetaking.domain.model.Note
import com.kaygv.notetaking.ui.dialog.folderDialog.FolderDialog
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteDialog
import com.kaygv.notetaking.ui.mvi.MviState

data class FolderState(
    val newFolderName: String = "",
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFolder: FolderWithNotes? = null,
    val noteDialog: NoteDialog = NoteDialog.None,
    val selectedNote: Note? = null,

    val dialog: FolderDialog = FolderDialog.None
) : MviState
