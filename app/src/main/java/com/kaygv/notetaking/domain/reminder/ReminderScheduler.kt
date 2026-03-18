package com.kaygv.notetaking.domain.reminder

interface ReminderScheduler {
    fun scheduleReminder(
        noteId: Long,
        triggerTime: Long
    )

    fun cancelReminder(noteId: Long)
}