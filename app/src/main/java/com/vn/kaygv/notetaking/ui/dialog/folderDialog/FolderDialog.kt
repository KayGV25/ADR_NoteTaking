package com.vn.kaygv.notetaking.ui.dialog.folderDialog

import com.vn.kaygv.notetaking.domain.model.Folder

sealed class FolderDialog {
    data object None : FolderDialog()
    data class Rename(val folder: Folder) : FolderDialog()
    data class Delete(val folder: Folder) : FolderDialog()
    data class Menu(val folder: Folder) : FolderDialog()
    data object Create : FolderDialog()
}
