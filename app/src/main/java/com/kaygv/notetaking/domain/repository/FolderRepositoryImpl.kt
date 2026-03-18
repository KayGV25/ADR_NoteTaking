package com.kaygv.notetaking.domain.repository

import com.kaygv.notetaking.data.db.dao.FolderDao
import com.kaygv.notetaking.data.mapper.toDomain
import com.kaygv.notetaking.data.mapper.toEntity
import com.kaygv.notetaking.domain.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FolderRepositoryImpl(
    private val folderDao: FolderDao
): FolderRepository {
    override fun getFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders()
            .map { folders -> folders.map { it.toDomain() } }
    }

    override suspend fun getFolderById(id: Long): Folder? {
        return folderDao.getFolderById(id)?.toDomain()
    }

    override fun getFoldersByName(name: String): Flow<List<Folder>> {
        return folderDao.getFoldersByName(name)
            .map { folders -> folders.map { it.toDomain() } }
    }

    override suspend fun createFolder(folder: Folder) {
        folderDao.insert(folder.toEntity())
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.update(folder.toEntity())
    }

    override suspend fun deleteFolder(folder: Folder) {
        folderDao.delete(folder.toEntity())
    }
}