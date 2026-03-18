package com.kaygv.notetaking.data.mapper

import com.kaygv.notetaking.data.db.entity.FolderEntity
import com.kaygv.notetaking.domain.model.Folder

fun FolderEntity.toDomain(): Folder {
    return Folder(
        id = id,
        name = name,
        createdAt = createdAt
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )
}