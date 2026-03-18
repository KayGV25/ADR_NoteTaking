package com.kaygv.notetaking.ui.editor

import com.kaygv.notetaking.ui.mvi.MviEvent

sealed class EditorEvent : MviEvent {
    data object NoteSaved : EditorEvent()
    data object NoteDeleted : EditorEvent()
}