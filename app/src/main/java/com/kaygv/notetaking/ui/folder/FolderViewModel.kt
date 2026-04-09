package com.kaygv.notetaking.ui.folder

import androidx.lifecycle.viewModelScope
import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.domain.repository.FolderRepository
import com.kaygv.notetaking.domain.repository.NoteRepository
import com.kaygv.notetaking.ui.dialog.folderDialog.FolderDialog
import com.kaygv.notetaking.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val repo: FolderRepository,
    private val noteRepo: NoteRepository
) : BaseViewModel<FolderIntent, FolderState, FolderEvent>(
    FolderState()
) {
    private val searchQueryFlow = MutableStateFlow("")
    private val _folders = MutableStateFlow<List<FolderWithNotes>>(emptyList())

    val folder: StateFlow<List<FolderWithNotes>> = _folders

    init {
        observeFolders()
    }

    override fun processIntent(intent: FolderIntent) {
        when (intent) {
            is FolderIntent.UpdateName -> updateName(intent.name)
            is FolderIntent.CreateFolder -> createFolder()
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

    private fun createFolder() {
        viewModelScope.launch {
            repo.createFolder(
                Folder(
                    name = state.value.newFolderName,
                    createdAt = System.currentTimeMillis()
                )
            )
            sendEvent(FolderEvent.FolderCreated)
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