package com.vn.kaygv.notetaking.ui.folder

import com.vn.kaygv.notetaking.domain.model.Folder
import com.vn.kaygv.notetaking.domain.model.Note

data class FolderWithNotes(
    val folder: Folder,
    val notes: List<Note>
)