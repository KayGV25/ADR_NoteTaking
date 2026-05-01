package com.vn.kaygv.notetaking.ui.folder

import androidx.lifecycle.viewModelScope
import com.vn.kaygv.notetaking.domain.model.Folder
import com.vn.kaygv.notetaking.domain.repository.FolderRepository
import com.vn.kaygv.notetaking.domain.repository.NoteRepository
import com.vn.kaygv.notetaking.domain.repository.ReminderRepository
import com.vn.kaygv.notetaking.ui.dialog.folderDialog.FolderDialog
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteActionHandler
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteDialog
import com.vn.kaygv.notetaking.ui.mvi.BaseViewModel
import com.vn.kaygv.notetaking.utils.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val repo: FolderRepository,
    private val reminderRepo: ReminderRepository,
    private val noteRepo: NoteRepository
) : BaseViewModel<FolderIntent, FolderState, FolderEvent>(
    FolderState()
) {
    private val searchQueryFlow = MutableStateFlow("")
    private val _folders = MutableStateFlow<List<FolderWithNotes>>(emptyList())

    val folder: StateFlow<List<FolderWithNotes>> = _folders
    private val noteActionHandler = NoteActionHandler(noteRepo, reminderRepo, repo)


    init {
        observeFolders()
    }

    override fun processIntent(intent: FolderIntent) {
        when (intent) {
            is FolderIntent.UpdateName -> updateName(intent.name)
            is FolderIntent.DeleteFolder -> deleteFolder(intent.folder)
            is FolderIntent.SearchFolders -> {
                setState {
                    copy(searchQuery = intent.query)
                }
                searchQueryFlow.value = intent.query
            }

            is FolderIntent.ConfirmDelete -> confirmDeleteFolder()
            is FolderIntent.ConfirmRename -> confirmRenameFolder()
            is FolderIntent.DismissDialog -> {
                setState {
                    copy(dialog = FolderDialog.None)
                }
            }
            is FolderIntent.OnLongPressFolder -> {
                setState {
                    copy(dialog = FolderDialog.Menu(intent.folder))
                }
            }
            is FolderIntent.OpenDeleteDialog -> {
                setState {
                    copy(
                        dialog = FolderDialog.Delete(intent.folder)
                    )
                }
            }
            is FolderIntent.OpenRenameDialog -> {
                setState {
                    copy(
                        dialog = FolderDialog.Rename(intent.folder),
                        newFolderName = intent.folder.name
                    )
                }
            }

            is FolderIntent.OpenNoteMenu -> {
                viewModelScope.launch {
                    val reminder = reminderRepo.getReminder(intent.note.id)

                    setState {
                        copy(
                            selectedNote = intent.note,
                            isMenuVisible = true,
                            reminderTime = reminder.reminderAt
                        )
                    }
                }
            }

            is FolderIntent.DismissNoteDialog -> {
                setState {
                    copy(
                    selectedNote = null,
                    noteDialog = NoteDialog.None
                    )
                }
            }

            is FolderIntent.NoteActionIntent -> {
                viewModelScope.launch {
                    noteActionHandler.handle(intent.action)
                }
            }

            is FolderIntent.DismissFolderBottomSheet -> {
                setState {
                    copy(
                        selectedFolder = null,
                        noteDialog = NoteDialog.None,
                        selectedNote = null
                    )
                }
            }

            is FolderIntent.OpenFolderBottomSheet -> {
                setState { copy(selectedFolder = intent.selectedFolder) }
            }

            is FolderIntent.ConfirmCreate -> {
                confirmCreateFolder()
            }

            is FolderIntent.OpenCreateDialog -> {
                setState {
                    copy(
                        dialog = FolderDialog.Create,
                        newFolderName = ""
                    )
                }
            }

            is FolderIntent.CloseNoteMenu -> {
                setState {
                    copy(
                        selectedNote = null,
                        isMenuVisible = false,
                        noteDialog = NoteDialog.None
                    )
                }
            }

            is FolderIntent.RemoveReminder -> {
                viewModelScope.launch {
                    reminderRepo.deleteReminder(intent.noteId)
                }
            }

            is FolderIntent.DeleteNote -> {
                viewModelScope.launch {
                    val note = noteRepo.getNotesOnce().find { it.id == intent.noteId }
                    note?.let {
                        ImageStorage.deleteImagesFromContent(it.content.text)
                        noteRepo.deleteNote(it)
                    }
                }
            }

            is FolderIntent.OpenSetReminderPicker -> {
                val note = state.value.selectedNote!!
                setState {
                    copy(
                        noteDialog = NoteDialog.Reminder(
                            note.id,
                            note.title,
                            note.content.text,
                            state.value.reminderTime
                        )
                    )
                }
            }

            is FolderIntent.OpenFolderPicker -> {
                viewModelScope.launch {
                    val folders = repo.getFolders().first()
                    val note = state.value.selectedNote!!

                    setState {
                        copy(
                            noteDialog = NoteDialog.Folder(
                                folders,
                                note.id,
                                note.folderId
                            )
                        )
                    }
                }
            }
        }
    }

    private fun confirmDeleteFolder() {
        val dialog = state.value.dialog as? FolderDialog.Delete ?: return
        val folder = dialog.folder

        viewModelScope.launch {
            val notes = noteRepo.getNotesOnce()

            notes.filter { it.folderId == folder.id }
                .forEach {
                    noteRepo.updateNote(it.copy(folderId = null))
                }

            repo.deleteFolder(folder)

            setState { copy(dialog = FolderDialog.None) }
        }
    }

    private fun confirmRenameFolder() {
        val dialog = state.value.dialog as? FolderDialog.Rename ?: return
        val folder = dialog.folder

        viewModelScope.launch {
            repo.updateFolder(
                folder.copy(name = state.value.newFolderName)
            )
            setState { copy(dialog = FolderDialog.None) }
        }
    }


    private fun updateName(name: String) {
        setState {
            copy(newFolderName = name)
        }
    }

    private fun confirmCreateFolder() {
        val name = state.value.newFolderName.trim()
        if (name.isEmpty()) return

        viewModelScope.launch {
            repo.createFolder(
                Folder(
                    name = name,
                    createdAt = System.currentTimeMillis()
                )
            )

            setState {
                copy(
                    dialog = FolderDialog.None,
                    newFolderName = ""
                )
            }
        }
    }


    private fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repo.deleteFolder(folder)
        }
    }

    private fun observeFolders() {
        viewModelScope.launch {
            combine(
                repo.getFolders(),
                noteRepo.getNotes(),
                searchQueryFlow
                    .debounce(300)
                    .distinctUntilChanged()
            ) { folders, notes, query ->

                folders
                    .map { folder ->
                        val folderNotes = notes.filter {
                            it.folderId == folder.id
                        }

                        FolderWithNotes(
                            folder = folder,
                            notes = folderNotes
                        )
                    }
                    .filter {
                        it.folder.name.contains(query, ignoreCase = true)
                    }
            }.collect {
                _folders.value = it
            }
        }
    }

}