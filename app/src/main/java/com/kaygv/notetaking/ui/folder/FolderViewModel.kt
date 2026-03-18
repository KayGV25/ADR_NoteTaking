package com.kaygv.notetaking.ui.folder

import androidx.lifecycle.viewModelScope
import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.domain.repository.FolderRepository
import com.kaygv.notetaking.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val repo: FolderRepository
) : BaseViewModel<FolderIntent, FolderState, FolderEvent>(
    FolderState()
) {
    private val searchQueryFlow = MutableStateFlow("")

    init {
        processIntent(FolderIntent.LoadFolders)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }
    override fun processIntent(intent: FolderIntent) {
        when (intent) {
            is FolderIntent.LoadFolders -> loadFolders()
            is FolderIntent.UpdateName -> updateName(intent.name)
            is FolderIntent.CreateFolder -> createFolder()
            is FolderIntent.DeleteFolder -> deleteFolder(intent.folder)
            is FolderIntent.SearchFolders -> {
                searchQueryFlow.value = intent.query
            }
        }
    }

    private fun loadFolders() {
        viewModelScope.launch {
            repo.getFolders().collect {
                setState {
                    copy(folders = it)
                }
            }
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
                ))
            sendEvent(FolderEvent.FolderCreated)
        }
    }

    private fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repo.deleteFolder(folder)
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            repo.getFoldersByName(query).collect { folders ->
                setState {
                    copy(folders = folders)
                }
            }
        }
    }
}