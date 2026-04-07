package com.kaygv.notetaking.ui.folder

import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.domain.model.Note

data class FolderWithNotes(
    val folder: Folder,
    val notes: List<Note>
)