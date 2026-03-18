package com.kaygv.notetaking.ui.folder

import com.kaygv.notetaking.ui.mvi.MviEvent

sealed class FolderEvent : MviEvent {
    data object FolderCreated : FolderEvent()
}