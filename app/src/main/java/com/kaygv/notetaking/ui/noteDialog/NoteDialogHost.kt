package com.kaygv.notetaking.ui.noteDialog

import androidx.compose.runtime.Composable
import com.kaygv.notetaking.domain.reminder.ReminderConstants
import com.kaygv.notetaking.ui.components.FolderPickerBottomSheet
import com.kaygv.notetaking.ui.components.SetReminderPicker
import java.util.Calendar

@Composable
fun NoteDialogHost(
    dialog: NoteDialog,
    onDismiss: () -> Unit,
    onAction: (NoteAction) -> Unit
) {
    when (dialog) {

        is NoteDialog.None -> Unit

        is NoteDialog.Reminder -> {
            val now = Calendar.getInstance().timeInMillis

            val initialTime = dialog.currentTime
                ?.takeIf { it != ReminderConstants.NO_REMINDER }
                ?: now

            SetReminderPicker(
                initialTime = initialTime,
                onConfirm = {
                    onAction(
                        NoteAction.SetReminder(dialog.noteId, it)
                    )
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }

        is NoteDialog.Folder -> {
            FolderPickerBottomSheet(
                folders = dialog.folders, // pass from VM if needed
                newFolderName = "",
                isCreating = false,
                onStartCreate = {},
                onUpdateFolderName = {},
                onCreate = {},
                onSelect = {
                    onAction(
                        NoteAction.AssignFolder(dialog.noteId, it)
                    )
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
    }
}
