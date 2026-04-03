package com.kaygv.notetaking.domain.repository

import com.kaygv.notetaking.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    fun getNotesByName(name: String): Flow<List<Note>>
    suspend fun createNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun deleteNoteById(noteId: Long)
}
