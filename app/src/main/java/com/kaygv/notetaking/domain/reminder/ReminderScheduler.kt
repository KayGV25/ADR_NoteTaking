package com.kaygv.notetaking.domain.reminder

interface ReminderScheduler {
    fun scheduleReminder(
        noteId: Long,
        noteTitle: String?,
        noteContent: String?,
        triggerTime: Long
    )

    fun cancelReminder(noteId: Long)
}