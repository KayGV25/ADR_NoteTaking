package com.vn.kaygv.notetaking.ui.home

import com.google.android.gms.ads.nativead.NativeAd
import com.vn.kaygv.notetaking.domain.model.Note
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteDialog
import com.vn.kaygv.notetaking.ui.mvi.MviState

data class HomeState(
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val groupedNotes: List<Pair<String, List<Note>>> = emptyList(),
    val ads: List<NativeAd> = emptyList(),
    val feed: List<UiItem> = emptyList(),

    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedNote: Note? = null,
    val isMenuVisible: Boolean = false,
    val reminderTime: Long? = null,
    val newFolderName: String = "",
    val isCreatingFolder: Boolean = false,
    val dialog: NoteDialog = NoteDialog.None
) : MviState

