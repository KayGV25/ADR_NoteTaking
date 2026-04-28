package com.vn.kaygv.notetaking.domain.model

import com.vn.kaygv.notetaking.domain.reminder.ReminderConstants

data class Reminder(
    val id: Long = 0,
    val noteId: Long,
    val reminderAt: Long = ReminderConstants.NO_REMINDER,
)

fun Reminder.hasReminder(): Boolean {
    return reminderAt != ReminderConstants.NO_REMINDER
}


