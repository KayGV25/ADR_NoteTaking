package com.kaygv.notetaking.ui.home

import com.kaygv.notetaking.ui.mvi.MviEvent

sealed class HomeEvent : MviEvent {
    data class OpenNote(
        val noteId: Long
    ) : HomeEvent()
}