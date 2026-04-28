package com.vn.kaygv.notetaking.ui.dialog.noteDialog

import com.vn.kaygv.notetaking.domain.reminder.ReminderConstants
import com.vn.kaygv.notetaking.domain.repository.FolderRepository
import com.vn.kaygv.notetaking.domain.repository.NoteRepository
import com.vn.kaygv.notetaking.domain.repository.ReminderRepository
import com.vn.kaygv.notetaking.utils.ImageStorage
import com.vn.kaygv.notetaking.domain.model.Folder
import javax.inject.Inject

class NoteActionHandler @Inject constructor(
    private val noteRepo: NoteRepository,
    private val reminderRepo: ReminderRepository,
    private val folderRepo: FolderRepository
) {

    suspend fun handle(action: NoteAction) {
        when (action) {

            is NoteAction.SetReminder -> {
                reminderRepo.setReminder(
                    action.noteId,
                    action.noteTitle,
                    action.noteContent,
                    action.time)
            }

            is NoteAction.RemoveReminder -> {
                reminderRepo.setReminder(
                    action.noteId,
                    action.noteTitle,
                    action.noteContent,
                    ReminderConstants.NO_REMINDER
                )
            }

            is NoteAction.AssignFolder -> {
                val note = noteRepo.getNoteById(action.noteId) ?: return
                if (note.folderId == action.folderId) {
                    noteRepo.updateNote(note.copy(folderId = null))
                } else {
                    noteRepo.updateNote(note.copy(folderId = action.folderId))
                }
            }

            is NoteAction.Delete -> {
                ImageStorage.deleteImagesFromContent(action.noteContent)
                noteRepo.deleteNoteById(action.noteId)
                reminderRepo.deleteReminder(action.noteId)
            }

            is NoteAction.CreateFolderAndAssign -> {
                val folderId = folderRepo.createFolder(
                    Folder(
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
