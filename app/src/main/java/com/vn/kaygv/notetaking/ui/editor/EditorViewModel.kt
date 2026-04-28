package com.vn.kaygv.notetaking.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.vn.kaygv.notetaking.domain.model.Folder
import com.vn.kaygv.notetaking.domain.model.Note
import com.vn.kaygv.notetaking.domain.reminder.ReminderConstants
import com.vn.kaygv.notetaking.domain.repository.FolderRepository
import com.vn.kaygv.notetaking.domain.repository.NoteRepository
import com.vn.kaygv.notetaking.domain.repository.ReminderRepository
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteAction
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteActionHandler
import com.vn.kaygv.notetaking.ui.dialog.noteDialog.NoteDialog
import com.vn.kaygv.notetaking.ui.editor.markdown.EditorBlock
import com.vn.kaygv.notetaking.ui.editor.markdown.blocksToMarkdown
import com.vn.kaygv.notetaking.ui.editor.markdown.parseMarkdownToBlocks
import com.vn.kaygv.notetaking.ui.mvi.BaseViewModel
import com.vn.kaygv.notetaking.utils.ImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
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
    private val noteActionHandler = NoteActionHandler(repo, reminderRepo, folderRepo)

    init {
        observeAutosave()
    }

    override fun processIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadNote -> loadNote(intent.noteId)
            is EditorIntent.SetReminder -> setReminderTime(intent.reminderTime)
            is EditorIntent.RemoveReminder -> removeReminder()
            is EditorIntent.SaveNote -> {
                if (isBlocksEmpty(state.value.blocks)) {
                    sendEvent(EditorEvent.NoteSaved) // just exit
                    return
                }
                isExisting = true
                saveNote(exitAfter = true)
            }

            is EditorIntent.DeleteNote -> deleteNote(intent.noteId)
            is EditorIntent.OpenFolderPicker -> openFolderPicker()
            is EditorIntent.AssignToFolder -> assignToFolder(intent.folderId)
            is EditorIntent.StartCreateFolder -> setState { copy(isCreatingFolder = true) }
            is EditorIntent.UpdateNewFolderName -> setState { copy(newFolderName = intent.name) }
            is EditorIntent.CreateFolder -> createFolder()
            is EditorIntent.ToggleCheckbox -> toggleCheckbox(intent.id)
            is EditorIntent.CloseSetReminderPicker -> {
                setState { copy(dialog = NoteDialog.None, isSetReminderPickerVisible = false) }
            }

            is EditorIntent.OpenSetReminderPicker -> {
                val id = state.value.noteId ?: return
                setState { copy(dialog = NoteDialog.Reminder(
                    id,
                    state.value.title,
                    blocksToMarkdown(state.value.blocks),
                    state.value.reminderTime)) }
            }

            is EditorIntent.DismissDialog -> setState { copy(dialog = NoteDialog.None) }
            is EditorIntent.FormatBold -> formatCurrent("**")
            is EditorIntent.FormatItalic -> formatCurrent("*")
            is EditorIntent.FormatUnderline -> formatCurrent("__")
            is EditorIntent.InsertCheckbox -> insertCheckboxBlock()
            is EditorIntent.InsertBullet -> insertBulletBlock()
            is EditorIntent.InsertNumbered -> insertNumberedBlock()
            is EditorIntent.ToggleImagePicker -> {
                setState { copy(isImagePickerOpen = !state.value.isImagePickerOpen) }
            }

            is EditorIntent.ToggleLinkDialog -> {
                setState { copy(isLinkDialogOpen = !state.value.isLinkDialogOpen) }
            }

            is EditorIntent.InsertImage -> insertImageBlock(intent.uri)
            is EditorIntent.InsertLink -> insertLinkBlock(intent.text, intent.url)

            is EditorIntent.Indent -> indentBlock(intent.id)
            is EditorIntent.Outdent -> outdentBlock(intent.id)

        }
    }
    fun updateBlock(id: String, value: TextFieldValue) {
        val updated = state.value.blocks.toMutableList()
        val index = updated.indexOfFirst { it.id == id }
        if (index == -1) return
        val currentBlock = updated[index]

        setState {
            copy(
                currentBlockId = id,
                currentSelection = value.selection
            )
        }

        if (currentBlock is EditorBlock.Heading) {
            val cleanedText = value.text.replace("\n", " ")

            val newValue = if (cleanedText.isEmpty()) {
                TextFieldValue("", TextRange.Zero)
            } else {
                value.copy(text = cleanedText)
            }

            updated[index] = currentBlock.copy(value = newValue)

            val normalized = normalizeNumbering(updated)
            setState {
                copy(
                    blocks = normalized,
                    currentSelection = newValue.selection
                )
            }
            updateTyping(blocksToMarkdown(normalized))
            return
        }

        val text = value.text
        val numberedRegex = Regex("""^\s*(\d+)\.\s""")
        val newBlock = when {
            text.startsWith("### ") -> EditorBlock.Heading(
                id = currentBlock.id, level = 3,
                value = value.copy(
                    text = text.removePrefix("### "),
                    selection = TextRange((value.selection.start - 4).coerceAtLeast(0))
                )
            )

            text.startsWith("## ") -> EditorBlock.Heading(
                id = currentBlock.id, level = 2,
                value = value.copy(
                    text = text.removePrefix("## "),
                    selection = TextRange((value.selection.start - 3).coerceAtLeast(0))
                )
            )

            text.startsWith("# ") -> EditorBlock.Heading(
                id = currentBlock.id, level = 1,
                value = value.copy(
                    text = text.removePrefix("# "),
                    selection = TextRange((value.selection.start - 2).coerceAtLeast(0))
                )
            )

            text.startsWith("- [ ] ") && currentBlock !is EditorBlock.Checkbox -> EditorBlock.Checkbox(
                id = currentBlock.id, checked = false,
                value = value.copy(
                    text = text.removePrefix("- [ ] "),
                    selection = TextRange((value.selection.start - 6).coerceAtLeast(0))
                )
            )

            text.startsWith("- [x] ") && currentBlock !is EditorBlock.Checkbox -> EditorBlock.Checkbox(
                id = currentBlock.id, checked = true,
                value = value.copy(
                    text = text.removePrefix("- [x] "),
                    selection = TextRange((value.selection.start - 6).coerceAtLeast(0))
                )
            )

            // FIX: Guard removed for Bullet. When the block is already a Bullet and its
            // text starts with "- " (due to a race condition between block-type conversion
            // and Enter being pressed), we still strip the prefix and preserve indent.
            // Previously the guard `currentBlock !is EditorBlock.Bullet` caused "- " to be
            // stored as the bullet's content, which then appeared as "- - " when split.
            text.startsWith("- ") && currentBlock !is EditorBlock.Checkbox -> {
                val stripped = value.copy(
                    text = text.removePrefix("- "),
                    selection = TextRange((value.selection.start - 2).coerceAtLeast(0))
                )
                when (currentBlock) {
                    // Already a Bullet → copy to preserve indent and id
                    is EditorBlock.Bullet -> currentBlock.copy(value = stripped)
                    // Any other non-Checkbox block → convert to new Bullet
                    else -> EditorBlock.Bullet(id = currentBlock.id, value = stripped)
                }
            }

            // FIX: Guard removed for Numbered. Same race condition applies: if the block
            // is already Numbered and "N. " gets stored as its text content, we strip it
            // and preserve the block's existing number and indent.
            numberedRegex.containsMatchIn(text) -> {
                val match = numberedRegex.find(text)!!
                val number = match.groupValues[1].toInt()
                val prefixLength = match.value.length
                val stripped = value.copy(
                    text = text.removePrefix(match.value),
                    selection = TextRange((value.selection.start - prefixLength).coerceAtLeast(0))
                )
                when (currentBlock) {
                    // Already Numbered → copy to preserve indent and id
                    is EditorBlock.Numbered -> currentBlock.copy(value = stripped)
                    // Any other block → convert to Numbered
                    else -> EditorBlock.Numbered(
                        id = currentBlock.id,
                        number = number,
                        indent = 0,
                        value = stripped
                    )
                }
            }

            else -> updateBlockValue(currentBlock, value)
        }

        updated[index] = newBlock
        val normalized = if (newBlock is EditorBlock.Numbered) normalizeNumbering(updated) else updated
        setState {
            copy(
                blocks = normalized,
                currentSelection = value.selection
            )
        }
        updateTyping(blocksToMarkdown(normalized))
    }

    private fun formatCurrent(prefix: String) {
        val id = state.value.currentBlockId ?: return
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == id }
        if (index == -1) return
        val block = blocks[index]

        val value = when (block) {
            is EditorBlock.Paragraph -> block.value
            is EditorBlock.Heading -> block.value
            is EditorBlock.Bullet -> block.value
            is EditorBlock.Checkbox -> block.value
            else -> return
        }

        val text = value.text
        var start = value.selection.start
        var end = value.selection.end
        if (start > end) { val temp = start; start = end; end = temp }

        // 1. Expand boundaries to include all adjacent formatting markers (*, _, ~)
        var left = start
        while (left > 0 && text[left - 1] in "*_~") left--
        var right = end
        while (right < text.length && text[right] in "*_~") right++

        // 2. Identify the "core" content by looking for markers at the edges of this expanded range
        var contentStart = left
        while (contentStart < right && text[contentStart] in "*_~") contentStart++
        var contentEnd = right
        while (contentEnd > contentStart && text[contentEnd - 1] in "*_~") contentEnd--

        val content = text.substring(contentStart, contentEnd)
        val fullPrefix = text.substring(left, contentStart)
        val fullSuffix = text.substring(contentEnd, right)

        // 3. Analyze current style states based on markers
        val starCount = minOf(fullPrefix.count { it == '*' }, fullSuffix.count { it == '*' })
        val hasUnderline = fullPrefix.contains("__") && fullSuffix.contains("__")
        val hasStrike = fullPrefix.contains("~~") && fullSuffix.contains("~~")

        var bold = starCount >= 2
        var italic = starCount % 2 == 1
        var underline = hasUnderline
        var strike = hasStrike

        // 4. Toggle the requested style
        when (prefix) {
            "**" -> bold = !bold
            "*" -> italic = !italic
            "__" -> underline = !underline
            "~~" -> strike = !strike
        }

        // 5. Reconstruct the markers and text
        val resPrefix = StringBuilder()
        val totalStars = (if (bold) 2 else 0) + (if (italic) 1 else 0)
        // Stars go first to keep bold+italic together (***)
        if (totalStars > 0) resPrefix.append("*".repeat(totalStars))
        if (underline) resPrefix.append("__")
        if (strike) resPrefix.append("~~")

        val resSuffix = if (underline || strike) {
            // Reverse order for suffix if nested
            val s = StringBuilder()
            if (strike) s.append("~~")
            if (underline) s.append("__")
            if (totalStars > 0) s.append("*".repeat(totalStars))
            s.toString()
        } else {
            resPrefix.toString()
        }

        val newText = text.substring(0, left) + resPrefix.toString() + content + resSuffix + text.substring(right)

        // 6. Calculate new selection
        val newSelection = if (start == end && content.isEmpty()) {
            TextRange(left + resPrefix.length)
        } else {
            // Keep selection on the content
            TextRange(left + resPrefix.length, left + resPrefix.length + content.length)
        }

        val newValue = TextFieldValue(text = newText, selection = newSelection)
        blocks[index] = updateBlockValue(block, newValue)
        setState {
            copy(
                blocks = blocks,
                currentSelection = newSelection
            )
        }
        updateTyping(blocksToMarkdown(blocks))
    }

    fun splitBlock(id: String) {
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == id }
        if (index == -1) return

        val block = blocks[index]

        // FIX: Empty bullet or checkbox → exit the list type by replacing the block
        // in-place with a paragraph.
        val isEmptyListBlock = when (block) {
            is EditorBlock.Bullet -> block.value.text.isEmpty()
            is EditorBlock.Numbered -> block.value.text.isEmpty()
            is EditorBlock.Checkbox -> block.value.text.isEmpty()
            else -> false
        }
        if (isEmptyListBlock) {
            val paragraph = EditorBlock.Paragraph(value = TextFieldValue(""))
            blocks[index] = paragraph
            setState {
                copy(
                    blocks = blocks,
                    currentBlockId = paragraph.id,
                    currentSelection = TextRange.Zero
                )
            }
            updateTyping(blocksToMarkdown(blocks))
            return
        }

        // Normal split at cursor position
        val (before, after) = when (block) {
            is EditorBlock.Paragraph -> split(block.value)
            is EditorBlock.Heading -> split(block.value)
            is EditorBlock.Bullet -> split(block.value)
            is EditorBlock.Checkbox -> split(block.value)
            is EditorBlock.Numbered -> split(block.value)
            else -> return
        }

        val newBlock = when (block) {
            is EditorBlock.Paragraph -> EditorBlock.Paragraph(value = after)

            is EditorBlock.Heading -> {
                // Heading always splits into heading + paragraph
                blocks[index] = block.copy(value = before)
                val nb = EditorBlock.Paragraph(value = after)
                blocks.add(index + 1, nb)
                val finalBlocks = blocks.toList()
                setState {
                    copy(blocks = finalBlocks, currentBlockId = nb.id, currentSelection = TextRange.Zero)
                }
                updateTyping(blocksToMarkdown(finalBlocks))
                return
            }

            is EditorBlock.Bullet -> EditorBlock.Bullet(
                indent = block.indent,
                value = after
            )
            is EditorBlock.Numbered -> EditorBlock.Numbered(
                value = after,
                number = block.number + 1,
                indent = block.indent
            )

            is EditorBlock.Checkbox -> EditorBlock.Checkbox(checked = false, value = after)

        }

        blocks[index] = updateBlockValue(block, before)
        blocks.add(index + 1, newBlock)

        val normalized = normalizeNumbering(blocks)
        setState {
            copy(
                blocks = normalized,
                currentBlockId = newBlock.id,
                currentSelection = TextRange(0)
            )
        }
        updateTyping(blocksToMarkdown(normalized))
    }

    fun mergeWithPrevious(id: String) {
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == id }
        if (index <= 0) return

        val current = blocks[index]
        val prev = blocks[index - 1]

        if (current is EditorBlock.Image) {
            ImageStorage.delete(current.url)
            blocks.removeAt(index)

            val newIndex = (index - 1).coerceAtLeast(0)
            val newBlock = blocks.getOrNull(newIndex)

            setState {
                copy(
                    blocks = blocks,
                    currentBlockId = newBlock?.id,
                    currentSelection = TextRange.Zero
                )
            }
            updateTyping(blocksToMarkdown(blocks))
            return
        }

        if (prev is EditorBlock.Image) {
            ImageStorage.delete(prev.url)
            blocks.removeAt(index - 1)

            setState {
                copy(
                    blocks = blocks,
                    currentBlockId = current.id,
                    currentSelection = TextRange.Zero
                )
            }
            updateTyping(blocksToMarkdown(blocks))
            return
        }

        val currentText = extractText(current)

        // CASE 1: Current block is empty → delete it, move cursor to end of previous.
        // We write the new selection into the prev block's TextFieldValue so that
        // LaunchedEffect(value) in BlockTextField detects the change and syncs internalValue.
        if (currentText.isEmpty()) {
            blocks.removeAt(index)
            val prevText = extractText(prev)
            val cursorAtEnd = TextRange(prevText.length)
            val updatedPrev = updateBlockValue(prev, TextFieldValue(prevText, cursorAtEnd))
            blocks[index - 1] = updatedPrev
            setState {
                copy(
                    blocks = blocks,
                    currentBlockId = updatedPrev.id,
                    currentSelection = cursorAtEnd
                )
            }
            updateTyping(blocksToMarkdown(blocks))
            return
        }

        // CASE 2: Current block has content → merge text into previous block.
        val prevText = extractText(prev)
        val mergedText = prevText + currentText
        val cursorAfterMerge = TextRange(prevText.length)

        val updatedPrev = when (prev) {
            is EditorBlock.Paragraph -> prev.copy(
                value = TextFieldValue(
                    mergedText,
                    cursorAfterMerge
                )
            )

            is EditorBlock.Heading -> prev.copy(
                value = TextFieldValue(
                    mergedText,
                    cursorAfterMerge
                )
            )

            is EditorBlock.Bullet -> prev.copy(value = TextFieldValue(mergedText, cursorAfterMerge))
            is EditorBlock.Numbered -> prev.copy(
                value = TextFieldValue(
                    mergedText,
                    cursorAfterMerge
                )
            )

            is EditorBlock.Checkbox -> prev.copy(
                value = TextFieldValue(
                    mergedText,
                    cursorAfterMerge
                )
            )
        }

        blocks[index - 1] = updatedPrev
        blocks.removeAt(index)

        setState {
            copy(
                blocks = blocks,
                currentBlockId = updatedPrev.id,
                currentSelection = cursorAfterMerge
            )
        }
        updateTyping(blocksToMarkdown(blocks))
    }

    private fun updateBlockValue(block: EditorBlock, newValue: TextFieldValue): EditorBlock =
        when (block) {
            is EditorBlock.Paragraph -> block.copy(value = newValue)
            is EditorBlock.Heading -> block.copy(value = newValue)
            is EditorBlock.Bullet -> block.copy(value = newValue)
            is EditorBlock.Numbered -> block.copy(value = newValue)
            is EditorBlock.Checkbox -> block.copy(value = newValue)
            else -> block
        }

    private fun split(value: TextFieldValue): Pair<TextFieldValue, TextFieldValue> {
        val cursor = value.selection.start
        return TextFieldValue(value.text.substring(0, cursor)) to
                TextFieldValue(value.text.substring(cursor))
    }

    private fun extractText(block: EditorBlock): String = when (block) {
        is EditorBlock.Paragraph -> block.value.text
        is EditorBlock.Heading -> block.value.text
        is EditorBlock.Bullet -> block.value.text
        is EditorBlock.Numbered -> block.value.text
        is EditorBlock.Checkbox -> block.value.text
        else -> ""
    }


    fun handleAction(action: NoteAction) {
        viewModelScope.launch { noteActionHandler.handle(action) }
    }

    private fun loadNote(noteId: Long) {
        viewModelScope.launch {
            val reminder = reminderRepo.getReminder(noteId)
            repo.getNoteById(noteId)?.let { note ->
                val parsedBlocks = parseMarkdownToBlocks(note.content.text)
                setState {
                    copy(
                        noteId = note.id,
                        title = note.title,
                        blocks = parsedBlocks,
                        currentBlockId = parsedBlocks.lastOrNull()?.id,
                        reminderTime = reminder.reminderAt,
                        folderId = note.folderId,
                        createdAt = note.createdAt
                    )
                }
            }
        }
    }

    private fun setReminderTime(time: Long) {
        viewModelScope.launch {
            state.value.noteId?.let { id ->
                reminderRepo.setReminder(
                    id,
                    state.value.title,
                    blocksToMarkdown(state.value.blocks),
                    time)
                setState { copy(reminderTime = time) }
            }
        }
    }

    private fun removeReminder() {
        viewModelScope.launch {
            state.value.noteId?.let { id ->
                reminderRepo.deleteReminder(id)
                setState { copy(reminderTime = ReminderConstants.NO_REMINDER) }
            }
        }
    }

    private fun saveNote(exitAfter: Boolean = false) {
        viewModelScope.launch {
            val markdown = blocksToMarkdown(state.value.blocks)
            if (isBlocksEmpty(state.value.blocks)) {
                // If this is a new note → just exit without saving
                if (state.value.noteId == null) {
                    if (exitAfter) sendEvent(EditorEvent.NoteSaved)
                    return@launch
                }

                // If note already exists → delete it (optional UX choice)
                repo.deleteNoteById(state.value.noteId!!)
                if (exitAfter) sendEvent(EditorEvent.NoteDeleted)
                return@launch
            }

            val title = extractTitle(markdown)

            val note = Note(
                id = state.value.noteId ?: 0,
                title = title,
                content = TextFieldValue(markdown),
                folderId = state.value.folderId,
                createdAt = state.value.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val id = if (note.id == 0L) repo.createNote(note)
            else {
                repo.updateNote(note)
                note.id
            }

            if (state.value.noteId == null) setState { copy(noteId = id) }
            if (exitAfter) sendEvent(EditorEvent.NoteSaved)
        }
    }

    private fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            val note = repo.getNoteById(noteId) ?: return@launch
            ImageStorage.deleteImagesFromContent(note.content.text)
            repo.deleteNoteById(noteId)
            sendEvent(EditorEvent.NoteDeleted)
        }
    }

    private fun extractTitle(content: String): String {
        val firstLine = content.lines().firstOrNull { it.isNotBlank() } ?: "Untitled"
        return firstLine
            .removePrefix("### ").removePrefix("## ").removePrefix("# ")
            .removePrefix("- [ ] ").removePrefix("- [x] ").removePrefix("- ")
            .take(30)
    }

    @OptIn(FlowPreview::class)
    private fun observeAutosave() {
        viewModelScope.launch {
            typingFlow.debounce(50).collect {
                if (!isBlocksEmpty(state.value.blocks)) {
                    saveNote()
                }
            }
        }
    }

    private fun updateTyping(content: String) {
        typingFlow.value = content
    }

    private fun openFolderPicker() {
        viewModelScope.launch {
            val folders = folderRepo.getFolders().firstOrNull() ?: emptyList()
            setState {
                copy(
                    folders = folders,
                    dialog = NoteDialog.Folder(
                        folders,
                        state.value.noteId ?: 0L,
                        state.value.folderId
                    )
                )
            }
        }
    }

    private fun assignToFolder(folderId: Long?) {
        setState { copy(folderId = folderId, dialog = NoteDialog.None) }
        saveNote()
    }

    private fun createFolder() {
        viewModelScope.launch {
            val id = folderRepo.createFolder(
                Folder(name = state.value.newFolderName, createdAt = System.currentTimeMillis())
            )
            assignToFolder(id)
            setState { copy(isCreatingFolder = false, newFolderName = "") }
        }
    }

    fun toggleCheckbox(id: String) {
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == id }
        if (index == -1) return
        val block = blocks[index] as? EditorBlock.Checkbox ?: return
        blocks[index] = block.copy(checked = !block.checked)
        setState { copy(blocks = blocks) }
        updateTyping(blocksToMarkdown(blocks))
    }

    fun insertCheckboxBlock() {
        insertBlockBelowCurrent(EditorBlock.Checkbox(checked = false, value = TextFieldValue("")))
    }

    fun insertBulletBlock() {
        insertBlockBelowCurrent(EditorBlock.Bullet(value = TextFieldValue("")))
    }

    fun insertNumberedBlock() {
        val currentId = state.value.currentBlockId
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == currentId }

        val number = calculateNextNumber(blocks, index)

        val newBlock = EditorBlock.Numbered(
            number = number,
            value = TextFieldValue("")
        )

        if (index == -1) blocks.add(newBlock)
        else blocks.add(index + 1, newBlock)

        setState {
            copy(
                blocks = blocks,
                currentBlockId = newBlock.id,
                currentSelection = TextRange.Zero
            )
        }
    }

    private fun calculateNextNumber(blocks: List<EditorBlock>, index: Int): Int {
        if (index < 0) return 1

        val prev = blocks.getOrNull(index)
        return if (prev is EditorBlock.Numbered) prev.number + 1 else 1
    }


    private fun insertImageBlock(url: String) {
        val imageBlock = EditorBlock.Image(url = url)
        val paragraphBlock = EditorBlock.Paragraph(value = TextFieldValue(""))

        val currentId = state.value.currentBlockId
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == currentId }

        if (index == -1) {
            blocks.add(imageBlock)
            blocks.add(paragraphBlock)
        } else {
            blocks.add(index + 1, imageBlock)
            blocks.add(index + 2, paragraphBlock)
        }

        setState {
            copy(
                blocks = blocks,
                currentBlockId = paragraphBlock.id,
                currentSelection = TextRange.Zero
            )
        }
        updateTyping(blocksToMarkdown(blocks))
    }

    fun insertLinkBlock(text: String, url: String) {
        val currentId = state.value.currentBlockId ?: return
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == currentId }
        if (index == -1) return

        val block = blocks[index]
        val value = when (block) {
            is EditorBlock.Paragraph -> block.value
            is EditorBlock.Heading -> block.value
            is EditorBlock.Bullet -> block.value
            is EditorBlock.Checkbox -> block.value
            else -> return
        }

        val linkMd = "[$text]($url)"
        val newText = value.text.replaceRange(value.selection.start, value.selection.end, linkMd)
        val newSelection = TextRange(value.selection.start + linkMd.length)
        val newValue = value.copy(text = newText, selection = newSelection)

        blocks[index] = updateBlockValue(block, newValue)
        setState { copy(blocks = blocks) }
        updateTyping(blocksToMarkdown(blocks))
    }

    private fun insertBlockBelowCurrent(newBlock: EditorBlock) {
        val currentId = state.value.currentBlockId
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == currentId }

        if (index == -1) blocks.add(newBlock)
        else blocks.add(index + 1, newBlock)

        setState {
            copy(
                blocks = blocks,
                currentBlockId = newBlock.id,
                currentSelection = TextRange.Zero
            )
        }
        updateTyping(blocksToMarkdown(blocks))
    }

    private fun normalizeNumbering(blocks: List<EditorBlock>): List<EditorBlock> {
        val counters = mutableMapOf<Int, Int>()

        return blocks.map { block ->
            if (block is EditorBlock.Numbered) {
                val level = block.indent
                val next = (counters[level] ?: 0) + 1
                counters[level] = next

                // reset deeper levels
                counters.keys.filter { it > level }.forEach { counters[it] = 0 }

                block.copy(number = next)
            } else {
                counters.clear() // Reset numbering when list is interrupted
                block
            }
        }
    }

    fun indentBlock(id: String) {
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == id }
        if (index == -1) return

        when (val block = blocks[index]) {
            is EditorBlock.Bullet -> blocks[index] = block.copy(indent = block.indent + 1)
            is EditorBlock.Numbered -> blocks[index] = block.copy(indent = block.indent + 1)
            is EditorBlock.Checkbox -> blocks[index] = block.copy(indent = block.indent + 1)
            else -> return
        }

        setState { copy(blocks = normalizeNumbering(blocks)) }
    }

    fun outdentBlock(id: String) {
        val blocks = state.value.blocks.toMutableList()
        val index = blocks.indexOfFirst { it.id == id }
        if (index == -1) return

        when (val block = blocks[index]) {
            is EditorBlock.Bullet -> {
                blocks[index] = block.copy(indent = (block.indent - 1).coerceAtLeast(0))
            }
            is EditorBlock.Numbered -> {
                blocks[index] = block.copy(indent = (block.indent - 1).coerceAtLeast(0))
            }
            else -> return
        }

        setState { copy(blocks = normalizeNumbering(blocks)) }
    }

    private fun isBlocksEmpty(blocks: List<EditorBlock>): Boolean {
        return blocks.all { block ->
            when (block) {
                is EditorBlock.Paragraph -> block.value.text.isBlank()
                is EditorBlock.Heading -> block.value.text.isBlank()
                is EditorBlock.Bullet -> block.value.text.isBlank()
                is EditorBlock.Numbered -> block.value.text.isBlank()
                is EditorBlock.Checkbox -> block.value.text.isBlank()
                is EditorBlock.Image -> false // image = not empty
            }
        }
    }
}