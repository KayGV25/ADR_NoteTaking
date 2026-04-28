package com.vn.kaygv.notetaking.data.mapper

import com.vn.kaygv.notetaking.data.db.entity.ReminderEntity
import com.vn.kaygv.notetaking.domain.model.Reminder

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        noteId = noteId,
        reminderAt = remindAt
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        noteId = noteId,
        remindAt = reminderAt
    )
}