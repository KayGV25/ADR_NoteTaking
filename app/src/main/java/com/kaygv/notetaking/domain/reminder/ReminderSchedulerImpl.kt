package com.kaygv.notetaking.domain.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kaygv.notetaking.data.reminder.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {
    private val workManager by lazy { WorkManager.getInstance(context) }

    override fun scheduleReminder(
        noteId: Long,
        triggerTime: Long
    ) {
        if (triggerTime == ReminderConstants.NO_REMINDER) return

        val delay = triggerTime - System.currentTimeMillis()

        if (delay <= 0) return

        val data = workDataOf(
            "noteId" to noteId
        )

        val request =
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

        workManager.enqueueUniqueWork(
            "note_reminder_$noteId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun cancelReminder(noteId: Long) {

        workManager.cancelUniqueWork(
            "note_reminder_$noteId"
        )
    }

}