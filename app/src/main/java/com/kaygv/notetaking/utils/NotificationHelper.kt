package com.kaygv.notetaking.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.kaygv.notetaking.MainActivity
import com.kaygv.notetaking.R

object NotificationHelper {

    private const val CHANNEL_ID = "note_reminders"

    fun showReminderNotification(
        context: Context,
        noteId: Long
    ) {

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_note_id", noteId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                noteId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications_active_24px)
                .setContentTitle("Note Reminder")
                .setContentText("Tap to open your note")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        manager.notify(noteId.toInt(), notification)
    }
}

