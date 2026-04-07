package com.kaygv.notetaking.ui.folder

import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.ui.mvi.MviIntent

sealed class FolderIntent : MviIntent {
    data class UpdateName(
        val name: String
    ) : FolderIntent()

    data object CreateFolder : FolderIntent()

    data class DeleteFolder(val folder: Folder) : FolderIntent()

    data class SearchFolders(val query: String) : FolderIntent()
}