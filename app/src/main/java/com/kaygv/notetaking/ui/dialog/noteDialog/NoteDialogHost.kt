package com.kaygv.notetaking.ui.dialog.noteDialog

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
    var isCreating by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    val context = LocalContext.current

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
                        NoteAction.SetReminder(
                            dialog.noteId,
                            dialog.noteTitle,
                            dialog.noteContent,
                            it)
                    )
                    Toast.makeText(context, "Reminder set", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }

        is NoteDialog.Folder -> {
            FolderPickerBottomSheet(
                selectedNoteFolderId = dialog.selectedFolderId,
                folders = dialog.folders,
                newFolderName = newFolderName,
                isCreating = isCreating,

                onStartCreate = {
                    isCreating = true
                },

                onUpdateFolderName = {
                    newFolderName = it
                },

                onCreate = {
                    if (newFolderName.isNotBlank()) {
                        onAction(
                            NoteAction.CreateFolderAndAssign(
                                dialog.noteId,
                                newFolderName
                            )
                        )
                        onDismiss()
                    }
                },

                onSelect = {
                    onAction(
                        NoteAction.AssignFolder(dialog.noteId, it)
                    )
                    Toast.makeText(context, "Folder assigned", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
    }
}
