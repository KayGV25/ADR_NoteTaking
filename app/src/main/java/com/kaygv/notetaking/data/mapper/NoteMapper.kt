package com.kaygv.notetaking.data.mapper

import androidx.compose.ui.text.input.TextFieldValue
import com.kaygv.notetaking.data.db.entity.NoteEntity
import com.kaygv.notetaking.domain.model.Note

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = TextFieldValue(content),
        folderId = folderId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content.text,
        folderId = folderId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}