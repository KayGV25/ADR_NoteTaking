package com.kaygv.notetaking.ui.home

import com.kaygv.notetaking.domain.model.Note
import com.kaygv.notetaking.ui.mvi.MviState

data class HomeState(
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
) : MviState

