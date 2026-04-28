package com.vn.kaygv.notetaking.data.reminder

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vn.kaygv.notetaking.domain.repository.ReminderRepository
import com.vn.kaygv.notetaking.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderRepo: ReminderRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("ReminderWorker", "WORKER STARTED")
        val noteId = inputData.getLong("noteId", -1L)
        val noteTitle = inputData.getString("noteTitle") ?: ""
        val noteContent = inputData.getString("noteContent") ?: ""

        NotificationHelper.showReminderNotification(
            applicationContext,
            noteId,
            noteTitle,
            noteContent
        )
        reminderRepo.deleteReminder(noteId)

        return Result.success()
    }
}