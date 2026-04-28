package com.vn.kaygv.notetaking.ui.folder

import com.vn.kaygv.notetaking.ui.mvi.MviEvent

sealed class FolderEvent : MviEvent {
    data object FolderCreated : FolderEvent()
}