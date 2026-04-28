package com.vn.kaygv.notetaking.domain.repository

import com.vn.kaygv.notetaking.data.db.dao.FolderDao
import com.vn.kaygv.notetaking.data.mapper.toDomain
import com.vn.kaygv.notetaking.data.mapper.toEntity
import com.vn.kaygv.notetaking.domain.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FolderRepositoryImpl(
    private val folderDao: FolderDao
) : FolderRepository {
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

    override suspend fun createFolder(folder: Folder): Long {
        return folderDao.insert(folder.toEntity())
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.update(folder.toEntity())
    }

    override suspend fun deleteFolder(folder: Folder) {
        folderDao.delete(folder.toEntity())
    }

    override suspend fun deleteFolderById(folderId: Long) {
        folderDao.deleteById(folderId)
    }
}