package com.kaygv.notetaking.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.domain.model.Note
import com.kaygv.notetaking.domain.reminder.ReminderConstants
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repo: NoteRepository,
    private val reminderRepo: ReminderRepository,
    private val folderRepo: FolderRepository
) : BaseViewModel<EditorIntent, EditorState, EditorEvent>(
    EditorState()
) {
    private val typingFlow = MutableStateFlow("")
    private var isExisting: Boolean = false
    private val noteActionHandler = NoteActionHandler(repo, reminderRepo)

    init {
        observeAutosave()
    }

    override fun processIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadNote -> loadNote(intent.noteId)
            is EditorIntent.UpdateContent -> updateContent(intent.content)
            is EditorIntent.SetReminder -> setReminderTime(intent.reminderTime)
            is EditorIntent.RemoveReminder -> removeReminder()
            is EditorIntent.SaveNote -> {
                isExisting = true
                saveNote(exitAfter = true)
            }

            is EditorIntent.DeleteNote -> deleteNote(intent.noteId)
            is EditorIntent.OpenFolderPicker -> openFolderPicker()
            is EditorIntent.AssignToFolder -> assignToFolder(intent.folderId)
            is EditorIntent.StartCreateFolder -> {
                setState { copy(isCreatingFolder = true) }
            }

            is EditorIntent.UpdateNewFolderName -> {
                setState { copy(newFolderName = intent.name) }
            }

            is EditorIntent.CreateFolder -> createFolder()
            is EditorIntent.ToggleCheckbox -> toggleCheckbox(intent.lineIndex, intent.checked)
            is EditorIntent.CloseSetReminderPicker -> {
                setState {
                    copy(
                        dialog = NoteDialog.None,
                        isSetReminderPickerVisible = false
                    )
                }
            }

            is EditorIntent.OpenSetReminderPicker -> {
                val id = state.value.noteId ?: return
                setState {
                    copy(
                        dialog = NoteDialog.Reminder(id, state.value.reminderTime)
                    )
                }
            }

            is EditorIntent.DismissDialog -> {
                setState { copy(dialog = NoteDialog.None) }
            }
        }
    }

    fun handleAction(action: NoteAction) {
        viewModelScope.launch {
            noteActionHandler.handle(action)
        }
    }

    private fun loadNote(noteId: Long) {
        viewModelScope.launch {
            val note = repo.getNoteById(noteId)
            val reminder = reminderRepo.getReminder(noteId)

            note?.let {

                val normalizedContent = enforceTitleHeading(it.content.text)

                val cursor = normalizedContent
                    .lineSequence()
                    .first()
                    .length

                setState {
                    copy(
                        noteId = it.id,
                        title = extractTitle(normalizedContent),
                        content = TextFieldValue(
                            text = normalizedContent,
                            selection = TextRange(cursor)
                        ),
                        reminderTime = reminder.reminderAt,
                        folderId = it.folderId,
                        createdAt = it.createdAt,
                    )
                }
            }
        }
    }


    private fun updateContent(value: TextFieldValue) {
        val oldText = state.value.content.text
        var text = value.text
        val selection = value.selection

        val isDeleting = text.length < oldText.length

        if (!isDeleting && text.endsWith("\n")) {

            val lines = text.lines()
            val prev = lines.getOrNull(lines.size - 2) ?: ""

            if (prev.startsWith("- [ ]") && prev.length > 5) {

                val newText = "$text- [ ] "

                setState {
                    copy(
                        content = value.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        ),
                        title = extractTitle(newText)
                    )
                }

                updateTyping(newText)
                return
            }

            if (prev.startsWith("- [x]") && prev.length > 5) {

                val newText = "$text- [x] "

                setState {
                    copy(
                        content = value.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        ),
                        title = extractTitle(newText)
                    )
                }

                updateTyping(newText)
                return
            }

            if (prev.startsWith("- ") && prev.length > 2) {

                val newText = "$text- "

                setState {
                    copy(
                        content = value.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        ),
                        title = extractTitle(newText)
                    )
                }

                updateTyping(newText)
                return
            }

            if (prev == "- ") {

                val newText = text.dropLast(3)

                setState {
                    copy(
                        content = value.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        ),
                        title = extractTitle(newText)
                    )
                }

                updateTyping(newText)
                return
            }
        }

        if (!text.startsWith("# ")) {

            text = "# " + text.removePrefix("#").trimStart()

            val newStart = (selection.start + 1).coerceAtMost(text.length)

            setState {
                copy(
                    content = value.copy(
                        text = text,
                        selection = TextRange(newStart)
                    ),
                    title = extractTitle(text)
                )
            }

            updateTyping(text)
            return
        }

        val newSelection = if (selection.start < 2) {
            TextRange(2)
        } else {
            selection
        }

        setState {
            copy(
                content = value.copy(
                    text = text,
                    selection = newSelection
                ),
                title = extractTitle(text)
            )
        }

        updateTyping(text)
    }

    private fun setReminderTime(reminderTime: Long) {
        viewModelScope.launch {

            val id = state.value.noteId ?: return@launch

            reminderRepo.setReminder(id, reminderTime)

            setState {
                copy(reminderTime = reminderTime)
            }
        }
    }

    private fun removeReminder() {
        viewModelScope.launch {

            val id = state.value.noteId ?: return@launch

            reminderRepo.setReminder(
                id,
                ReminderConstants.NO_REMINDER
            )

            setState {
                copy(reminderTime = ReminderConstants.NO_REMINDER)
            }
        }
    }

    private fun saveNote(exitAfter: Boolean = false) {
        if (state.value.isSaving) return

        viewModelScope.launch {

            val current = state.value
            val now = System.currentTimeMillis()

            val text = current.content.text
            val meaningful = isMeaningfulContent(text)

            if (!meaningful) {
                current.noteId?.let {
                    repo.deleteNoteById(it)
                }

                if (exitAfter) {
                    sendEvent(EditorEvent.NoteDeleted)
                }
                return@launch
            }

            setState { copy(isSaving = true) }

            val note = Note(
                id = current.noteId ?: 0,
                title = current.title,
                content = current.content,
                folderId = current.folderId,
                createdAt = current.createdAt ?: now,
                updatedAt = now
            )

            if (current.noteId == null) {
                // 🔥 CREATE ONCE
                val newId = repo.createNote(note)

                setState {
                    copy(
                        noteId = newId,           // ✅ IMPORTANT
                        createdAt = now,
                        isSaving = false
                    )
                }

                isExisting = true // ✅ mark as existing
            } else {
                // 🔥 UPDATE ONLY
                repo.updateNote(note)

                setState {
                    copy(isSaving = false)
                }
            }

            if (exitAfter) {
                sendEvent(EditorEvent.NoteSaved)
            }
        }
    }

    private fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repo.deleteNoteById(noteId)
            sendEvent(EditorEvent.NoteDeleted)
        }
    }

    private fun extractTitle(content: String): String {
        val firstLine = content
            .lineSequence()
            .firstOrNull()
            ?.trim()
            ?: ""

        return if (firstLine.startsWith("# ")) {
            firstLine.removePrefix("# ").trim()
        } else {
            firstLine.trim()
        }.take(80)
    }

    private fun enforceTitleHeading(content: String): String {
        if (content.isBlank()) {
            return "# "
        }

        val lines = content.lines().toMutableList()
        val firstLine = lines[0]

        val cleaned = firstLine
            .removePrefix("#")
            .trim()

        lines[0] = "# $cleaned"

        return lines.joinToString("\n")
    }

    private fun isMeaningfulContent(content: String): Boolean {
        val cleaned = content
            .removePrefix("#")
            .trim()

        return cleaned.isNotEmpty()
    }

    @OptIn(FlowPreview::class)
    private fun observeAutosave() {
        viewModelScope.launch {
            typingFlow
                .debounce(300)
                .collect {
                    saveNote()
                }
        }
    }

    private fun updateTyping(text: String) {
        viewModelScope.launch {
            typingFlow.emit(text)
        }
    }

    private fun openFolderPicker() {
        viewModelScope.launch {
            val folders = folderRepo.getFolders().first()
            val id = state.value.noteId ?: return@launch

            setState {
                copy(
                    dialog = NoteDialog.Folder(folders, id),
                    folders = folders,
                    isFolderPickerVisible = true
                )
            }
        }
    }

    private fun assignToFolder(folderId: Long?) {
        setState {
            copy(
                folderId = folderId,
                isFolderPickerVisible = false
            )
        }

        saveNote()
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

            // 🔥 assign immediately
            assignToFolder(id)

            setState {
                copy(
                    newFolderName = "",
                    isCreatingFolder = false
                )
            }
        }
    }

    private fun toggleCheckbox(lineIndex: Int, checked: Boolean) {

        val lines = state.value.content.text.lines().toMutableList()

        if (lineIndex >= lines.size) return

        val line = lines[lineIndex]

        val newLine = when {

            line.startsWith("- [ ]") -> {
                "- [x]" + line.removePrefix("- [ ]")
            }

            line.startsWith("- [x]") -> {
                "- [ ]" + line.removePrefix("- [x]")
            }

            else -> return
        }

        lines[lineIndex] = newLine

        val newText = lines.joinToString("\n")

        setState {
            copy(
                content = state.value.content.copy(
                    text = newText,
                    selection = state.value.content.selection
                )
            )
        }
    }
}