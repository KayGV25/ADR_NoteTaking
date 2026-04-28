package com.vn.kaygv.notetaking.domain.repository

import com.vn.kaygv.notetaking.domain.model.Reminder

interface ReminderRepository {
    suspend fun getReminder(noteId: Long): Reminder

    suspend fun setReminder(noteId: Long, noteTitle: String?, noteContent: String?, time: Long)

    suspend fun deleteReminder(noteId: Long)
    suspend fun createReminder(reminder: Reminder)
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
}