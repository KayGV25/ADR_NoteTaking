package com.kaygv.notetaking.domain.repository

import com.kaygv.notetaking.data.db.dao.ReminderDao
import com.kaygv.notetaking.data.mapper.toDomain
import com.kaygv.notetaking.data.mapper.toEntity
import com.kaygv.notetaking.domain.model.Reminder
import com.kaygv.notetaking.domain.reminder.ReminderConstants
import com.kaygv.notetaking.domain.reminder.ReminderScheduler

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao,
    private val scheduler: ReminderScheduler
) : ReminderRepository {
    override suspend fun getReminder(noteId: Long): Reminder {
        val entity = reminderDao.getReminder(noteId)

        return entity?.toDomain() ?: Reminder(
            noteId = noteId,
            reminderAt = ReminderConstants.NO_REMINDER
        )
    }

    override suspend fun setReminder(noteId: Long, time: Long) {
        val reminder = Reminder(
            noteId = noteId,
            reminderAt = time
        )

        reminderDao.insert(reminder.toEntity())

        if (time == ReminderConstants.NO_REMINDER) {
            scheduler.cancelReminder(noteId)
        } else {
            scheduler.scheduleReminder(
                noteId = noteId,
                triggerTime = time
            )
        }
    }

    override suspend fun deleteReminder(noteId: Long) {
        reminderDao.deleteReminder(noteId)

        scheduler.cancelReminder(noteId)
    }

    override suspend fun createReminder(reminder: Reminder) {
        reminderDao.insert(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.update(reminder.toEntity())
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.delete(reminder.toEntity())
    }
}