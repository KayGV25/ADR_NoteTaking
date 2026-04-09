package com.kaygv.notetaking.ui.dialog.noteDialog

import com.kaygv.notetaking.domain.reminder.ReminderConstants
import com.kaygv.notetaking.domain.repository.FolderRepository
import com.kaygv.notetaking.domain.repository.NoteRepository
import com.kaygv.notetaking.domain.repository.ReminderRepository
import javax.inject.Inject

class NoteActionHandler @Inject constructor(
    private val noteRepo: NoteRepository,
    private val reminderRepo: ReminderRepository,
    private val folderRepo: FolderRepository
) {

    suspend fun handle(action: NoteAction) {
        when (action) {

            is NoteAction.SetReminder -> {
                reminderRepo.setReminder(action.noteId, action.time)
            }

            is NoteAction.RemoveReminder -> {
                reminderRepo.setReminder(
                    action.noteId,
                    ReminderConstants.NO_REMINDER
                )
            }

            is NoteAction.AssignFolder -> {
                val note = noteRepo.getNoteById(action.noteId) ?: return
                noteRepo.updateNote(note.copy(folderId = action.folderId))
            }

            is NoteAction.Delete -> {
                noteRepo.deleteNoteById(action.noteId)
                reminderRepo.setReminder(
                    action.noteId,
                    ReminderConstants.NO_REMINDER
                )
            }

            is NoteAction.CreateFolderAndAssign -> {
                val folderId = folderRepo.createFolder(
                    com.kaygv.notetaking.domain.model.Folder(
                        name = action.folderName,
                        createdAt = System.currentTimeMillis()
                    )
                )

                val note = noteRepo.getNoteById(action.noteId) ?: return
                noteRepo.updateNote(note.copy(folderId = folderId))
            }

        }
    }
}
