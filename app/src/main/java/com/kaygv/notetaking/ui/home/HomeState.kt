package com.kaygv.notetaking.ui.home

import com.kaygv.notetaking.domain.model.Note
import com.kaygv.notetaking.ui.mvi.MviState
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteDialog

data class HomeState(
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val groupedNotes: List<Pair<String, List<Note>>> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedNote: Note? = null,
    val isMenuVisible: Boolean = false,
    val reminderTime: Long? = null,
    val newFolderName: String = "",
    val isCreatingFolder: Boolean = false,
    val dialog: NoteDialog = NoteDialog.None
) : MviState

