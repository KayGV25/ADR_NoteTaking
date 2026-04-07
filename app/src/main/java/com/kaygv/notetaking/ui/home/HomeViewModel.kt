package com.kaygv.notetaking.ui.home

import androidx.lifecycle.viewModelScope
import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.domain.repository.FolderRepository
import com.kaygv.notetaking.domain.repository.NoteRepository
import com.kaygv.notetaking.domain.repository.ReminderRepository
import com.kaygv.notetaking.ui.mvi.BaseViewModel
import com.kaygv.notetaking.ui.noteDialog.NoteAction
import com.kaygv.notetaking.ui.noteDialog.NoteActionHandler
import com.kaygv.notetaking.ui.noteDialog.NoteDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: NoteRepository,
    private val reminderRepo: ReminderRepository,
    private val folderRepo: FolderRepository
) : BaseViewModel<HomeIntent, HomeState, HomeEvent>(
    HomeState()
) {
    private val searchQueryFlow = MutableStateFlow("")
    private val noteActionHandler = NoteActionHandler(repo, reminderRepo)

    init {
        processIntent(HomeIntent.LoadNotes)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    override fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadNotes -> loadNotes()
            is HomeIntent.SearchNotes -> {
                setState {
                    copy(searchQuery = intent.query)
                }
                searchQueryFlow.value = intent.query
            }

            is HomeIntent.DeleteNote -> deleteNote(intent.noteId)
            is HomeIntent.AssignToFolder -> assignToFolder(intent.noteId, intent.folderId)
            is HomeIntent.OpenFolderPicker -> openFolderPicker()
            is HomeIntent.CloseFolderPicker -> {
                setState {
                    copy(
                        dialog = NoteDialog.None,
                    )
                }
            }

            is HomeIntent.StartCreateFolder -> {
                setState { copy(isCreatingFolder = true) }
            }

            is HomeIntent.UpdateNewFolderName -> {
                setState { copy(newFolderName = intent.name) }
            }

            is HomeIntent.CreateFolder -> createFolder()
            is HomeIntent.CloseNoteMenu -> {
                setState {
                    copy(
                        dialog = NoteDialog.None,
                        selectedNote = null,
                        isMenuVisible = false
                    )
                }
            }

            is HomeIntent.OpenNoteMenu -> {
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

            is HomeIntent.SetNoteReminder -> setReminder(intent.noteId, intent.reminderTime)
            is HomeIntent.RemoveReminder -> removeReminder(intent.noteId)
            is HomeIntent.CloseSetReminderPicker -> {
                setState {
                    copy(
                        dialog = NoteDialog.None,
                    )
                }
            }

            is HomeIntent.OpenSetReminderPicker -> {
                setState {
                    copy(
                        dialog = NoteDialog.Reminder(
                            state.value.selectedNote!!.id,
                            state.value.reminderTime
                        )
                    )
                }
            }

            is HomeIntent.DismissDialog -> {
                setState { copy(dialog = NoteDialog.None) }
            }
        }
    }

    fun handleAction(action: NoteAction) {
        viewModelScope.launch {
            noteActionHandler.handle(action)
        }
    }


    private fun loadNotes() {
        viewModelScope.launch {
            repo.getNotes().collect { notes ->
                setState {
                    copy(
                        notes = notes,
                        filteredNotes = notes
                    )
                }
            }
        }
    }

    private fun performSearch(query: String) {
        val filteredNotes = state.value.notes.filter {
            it.title.contains(query, ignoreCase = true)
                    || it.content.text.contains(query, ignoreCase = true)
        }

        setState {
            copy(
                searchQuery = query,
                filteredNotes = filteredNotes
            )
        }
    }

    private fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            val note = state.value.notes.find { it.id == noteId }
            note?.let {
                repo.deleteNote(it)
            }
        }
    }

    private fun setReminder(noteId: Long?, reminderTime: Long) {
        viewModelScope.launch {
            val note = state.value.notes.find { it.id == noteId }
            note?.let {
                reminderRepo.setReminder(note.id, reminderTime)
            }
        }
    }

    private fun removeReminder(noteId: Long) {
        viewModelScope.launch {
            reminderRepo.deleteReminder(noteId)
        }
    }

    private fun openFolderPicker() {
        viewModelScope.launch {
            val folders = folderRepo.getFolders().first()
            setState {
                copy(
                    dialog = NoteDialog.Folder(folders, state.value.selectedNote!!.id)
                )
            }
        }
    }

    private fun assignToFolder(noteId: Long, folderId: Long?) {
        viewModelScope.launch {
            val note = state.value.notes.find { it.id == noteId }
            note?.let {
                repo.updateNote(it.copy(folderId = folderId))
                setState {
                    copy(
                        dialog = NoteDialog.None,
                        selectedNote = null
                    )
                }
            }
        }
    }

    private fun createFolder() {
        viewModelScope.launch {
            val name = state.value.newFolderName.trim()
            if (name.isEmpty()) return@launch

            val folder = Folder(
                name = name,
                createdAt = System.currentTimeMillis()
            )

            val id = folderRepo.createFolder(folder)

            state.value.selectedNote?.let {
                assignToFolder(it.id, id)
            }

            setState {
                copy(
                    newFolderName = "",
                    isCreatingFolder = false
                )
            }
        }
    }
}
