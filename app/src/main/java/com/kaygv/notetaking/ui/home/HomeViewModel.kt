package com.kaygv.notetaking.ui.home

import androidx.lifecycle.viewModelScope
import com.kaygv.notetaking.domain.repository.NoteRepository
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
class HomeViewModel @Inject constructor(
    private val repo: NoteRepository
) : BaseViewModel<HomeIntent, HomeState, HomeEvent>(
    HomeState()
) {
    private val searchQueryFlow = MutableStateFlow("")

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

    private fun updateSearchQuery(searchQuery: String) {
        setState {
            copy(searchQuery = searchQuery)
        }
    }
}