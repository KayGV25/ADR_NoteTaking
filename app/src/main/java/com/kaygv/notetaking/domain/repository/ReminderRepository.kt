package com.kaygv.notetaking.domain.repository

import com.kaygv.notetaking.domain.model.Reminder

interface ReminderRepository {
    suspend fun getReminder(noteId: Long): Reminder

    suspend fun setReminder(noteId: Long, time: Long)

    suspend fun deleteReminder(noteId: Long)
    suspend fun createReminder(reminder: Reminder)
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
}