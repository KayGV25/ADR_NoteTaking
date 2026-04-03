package com.kaygv.notetaking.ui.folder

import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.ui.mvi.MviState

data class FolderState(
    val newFolderName: String = "",
    val isLoading: Boolean = false,
    val searchQuery: String = "",
) : MviState
