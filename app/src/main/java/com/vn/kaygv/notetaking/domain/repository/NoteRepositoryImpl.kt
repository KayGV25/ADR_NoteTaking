package com.vn.kaygv.notetaking.domain.repository

import com.vn.kaygv.notetaking.data.db.dao.NoteDao
import com.vn.kaygv.notetaking.data.mapper.toDomain
import com.vn.kaygv.notetaking.data.mapper.toEntity
import com.vn.kaygv.notetaking.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
            .map { notes -> notes.map { it.toDomain() } }
    }

    override suspend fun getNotesOnce(): List<Note> {
        return getNotes().first()
    }

    override suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }

    override fun getNotesByName(name: String): Flow<List<Note>> {
        return noteDao.getNotesByName(name)
            .map { notes -> notes.map { it.toDomain() } }
    }

    override suspend fun createNote(note: Note): Long {
        return noteDao.insert(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.update(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.delete(note.toEntity())
    }

    override suspend fun deleteNoteById(noteId: Long) {
        noteDao.deleteById(noteId)
    }
}
