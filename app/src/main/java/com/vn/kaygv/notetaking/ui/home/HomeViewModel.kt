package com.vn.kaygv.notetaking.ui.home

import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.vn.kaygv.notetaking.domain.model.Folder
import com.vn.kaygv.notetaking.domain.model.Note
import com.vn.kaygv.notetaking.domain.repository.FolderRepository
import com.vn.kaygv.notetaking.domain.repository.NoteRepository
import com.vn.kaygv.notetaking.domain.repository.ReminderRepository
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteAction
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteActionHandler
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteDialog
import com.vn.kaygv.notetaking.ui.mvi.BaseViewModel
import com.vn.kaygv.notetaking.utils.AdConfig
import com.vn.kaygv.notetaking.utils.AdLoaderManager
import com.vn.kaygv.notetaking.utils.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: NoteRepository,
    private val reminderRepo: ReminderRepository,
    private val folderRepo: FolderRepository,
    private val adLoaderManager: AdLoaderManager
) : BaseViewModel<HomeIntent, HomeState, HomeEvent>(
    HomeState()
) {
    private val searchQueryFlow = MutableStateFlow("")
    private val noteActionHandler = NoteActionHandler(repo, reminderRepo, folderRepo)

    init {
        processIntent(HomeIntent.LoadNotes)

        if (AdConfig.ENABLE_ADS) {
            loadAds()
        }
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        state.value.ads.forEach { it.destroy() }
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
                            state.value.selectedNote!!.title,
                            state.value.selectedNote!!.content.text,
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
                        filteredNotes = notes,
                        groupedNotes = groupNotes(notes)
                    )
                }
                buildFeed(notes, state.value.ads)
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
                filteredNotes = filteredNotes,
                groupedNotes = groupNotes(filteredNotes)
            )
        }
        buildFeed(filteredNotes, state.value.ads)
    }

    private fun groupNotes(notes: List<Note>): List<Pair<String, List<Note>>> {
        if (notes.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000L
        val thirtyDaysAgo = now - 30 * 24 * 60 * 60 * 1000L

        calendar.timeInMillis = now
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        calendar.add(Calendar.MONTH, -1)
        val startOfLastMonth = calendar.timeInMillis
        val lastMonthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)

        calendar.add(Calendar.MONTH, -1)
        val startOfNextLastMonth = calendar.timeInMillis
        val nextLastMonthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)

        val sortedNotes = notes.sortedByDescending { it.updatedAt }

        val prev7Days = mutableListOf<Note>()
        val prev30Days = mutableListOf<Note>()
        val lastMonthNotes = mutableListOf<Note>()
        val nextLastMonthNotes = mutableListOf<Note>()
        val yearlyNotes = mutableMapOf<Int, MutableList<Note>>()

        for (note in sortedNotes) {
            val time = note.updatedAt
            when {
                time >= sevenDaysAgo -> prev7Days.add(note)
                time >= thirtyDaysAgo -> prev30Days.add(note)
                time >= startOfLastMonth -> lastMonthNotes.add(note)
                time >= startOfNextLastMonth -> nextLastMonthNotes.add(note)
                else -> {
                    calendar.timeInMillis = time
                    val year = calendar.get(Calendar.YEAR)
                    yearlyNotes.getOrPut(year) { mutableListOf() }.add(note)
                }
            }
        }

        val result = mutableListOf<Pair<String, List<Note>>>()
        if (prev7Days.isNotEmpty()) result.add("Previous 7 days" to prev7Days)
        if (prev30Days.isNotEmpty()) result.add("Previous 30 days" to prev30Days)
        if (lastMonthNotes.isNotEmpty()) result.add(lastMonthName to lastMonthNotes)
        if (nextLastMonthNotes.isNotEmpty()) result.add(nextLastMonthName to nextLastMonthNotes)

        yearlyNotes.keys.sortedDescending().forEach { year ->
            result.add(year.toString() to yearlyNotes[year]!!)
        }

        return result
    }

    private fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            val note = state.value.notes.find { it.id == noteId }
            note?.let {
                ImageStorage.deleteImagesFromContent(it.content.text)
                repo.deleteNote(it)
            }
        }
    }

    private fun setReminder(noteId: Long?, reminderTime: Long) {
        viewModelScope.launch {
            val note = state.value.notes.find { it.id == noteId }
            note?.let {
                reminderRepo.setReminder(
                    note.id,
                    state.value.selectedNote?.title,
                    state.value.selectedNote?.content?.text,
                    reminderTime)
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
                    dialog = NoteDialog.Folder(
                        folders,
                        state.value.selectedNote!!.id,
                        state.value.selectedNote?.folderId)
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

    private fun loadAds() {
        if (!AdConfig.ENABLE_ADS) return

        viewModelScope.launch {
            val ads = adLoaderManager.loadAds(count = 5)
            setState { copy(ads = ads) }

            buildFeed(state.value.filteredNotes, ads)
        }
    }

    private fun buildFeed(notes: List<Note>, ads: List<NativeAd>) {

        val grouped = groupNotes(notes)
        val result = mutableListOf<UiItem>()

        val adInterval = 5
        var counter = 0
        var adIndex = 0

        grouped.forEach { (header, notesInGroup) ->
            result.add(UiItem.Header(header))

            notesInGroup.forEach { note ->
                result.add(UiItem.NoteItem(note))
                counter++

                if (
                    AdConfig.ENABLE_ADS &&
                    counter % adInterval == 0 &&
                    adIndex < ads.size
                ) {
                    result.add(UiItem.AdItem(ads[adIndex++]))
                }
            }
        }

        setState { copy(feed = result) }
    }
}
