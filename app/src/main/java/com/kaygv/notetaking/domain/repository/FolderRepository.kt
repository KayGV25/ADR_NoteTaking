package com.kaygv.notetaking.domain.repository

import com.kaygv.notetaking.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getFolders(): Flow<List<Folder>>
    suspend fun getFolderById(id: Long): Folder?
    fun getFoldersByName(name: String): Flow<List<Folder>>
    suspend fun createFolder(folder: Folder): Long
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folder: Folder)
    suspend fun deleteFolderById(folderId: Long)
}