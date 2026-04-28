package com.vn.kaygv.notetaking.ui.home

import com.vn.kaygv.notetaking.ui.mvi.MviEvent

sealed class HomeEvent : MviEvent {
    data class OpenNote(
        val noteId: Long
    ) : HomeEvent()
}