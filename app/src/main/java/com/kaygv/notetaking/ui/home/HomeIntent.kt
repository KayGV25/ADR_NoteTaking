package com.kaygv.notetaking.ui.home

import com.kaygv.notetaking.ui.mvi.MviIntent

sealed class HomeIntent: MviIntent {
    data object LoadNotes : HomeIntent()

    data class SearchNotes(val query: String) : HomeIntent()

    data class DeleteNote(val noteId: Long) : HomeIntent()
}