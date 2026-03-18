package com.kaygv.notetaking.ui.editor

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.kaygv.notetaking.domain.model.Note
import com.kaygv.notetaking.domain.reminder.ReminderConstants
import com.kaygv.notetaking.domain.repository.NoteRepository
import com.kaygv.notetaking.domain.repository.ReminderRepository
import com.kaygv.notetaking.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repo: NoteRepository,
    private val reminderRepo: ReminderRepository
) : BaseViewModel<EditorIntent, EditorState, EditorEvent>(
    EditorState()
) {
    private val typingFlow = MutableStateFlow("")

    init {
        observeAutosave()
    }

    override fun processIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadNote -> loadNote(intent.noteId)
            is EditorIntent.UpdateContent -> updateContent(intent.content)
            is EditorIntent.SetReminder -> setReminderTime(intent.reminderTime)
            is EditorIntent.RemoveReminder -> removeReminder()
            is EditorIntent.SaveNote -> saveNote(exitAfter = true)
            is EditorIntent.DeleteNote -> deleteNote(intent.noteId)
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
                            selection = androidx.compose.ui.text.TextRange(cursor)
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

        var text = value.text
        var selection = value.selection

        // Ensure prefix exists
        if (!text.startsWith("# ")) {
            text = "# " + text.removePrefix("#").trimStart()
        }

        // Prevent deleting "# "
        if (text.length < 2) {
            text = "# "
        }

        // Clamp cursor so it never goes before "# "
        val cursor = selection.start.coerceAtLeast(2)

        selection = androidx.compose.ui.text.TextRange(cursor)

        val title = extractTitle(text)

        setState {
            copy(
                content = TextFieldValue(
                    text = text,
                    selection = selection
                ),
                title = title
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

        viewModelScope.launch {

            val current = state.value
            val now = System.currentTimeMillis()

            val text = current.content.text

            val meaningful = isMeaningfulContent(text)

            if (!meaningful) {
                current.noteId?.let {
                    repo.deleteNoteById(it)
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
                repo.createNote(note)
            } else {
                repo.updateNote(note)
            }

            setState {
                copy(
                    isSaving = false,
                )
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
                .debounce(800)
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
}