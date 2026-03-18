package com.kaygv.notetaking.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kaygv.notetaking.utils.NotificationHelper

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {

        val noteId = inputData.getLong("noteId", -1)

        if (noteId == -1L) {
            return Result.failure()
        }

        NotificationHelper.showReminderNotification(
            context = applicationContext,
            noteId = noteId
        )

        return Result.success()
    }
}