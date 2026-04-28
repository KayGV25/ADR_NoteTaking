package com.vn.kaygv.notetaking.ui.editor

import com.vn.kaygv.notetaking.ui.mvi.MviEvent

sealed class EditorEvent : MviEvent {
    data object NoteSaved : EditorEvent()
    data object NoteDeleted : EditorEvent()
}