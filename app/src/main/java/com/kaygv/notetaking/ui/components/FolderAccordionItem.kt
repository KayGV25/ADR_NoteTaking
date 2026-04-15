package com.kaygv.notetaking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.ui.folder.FolderWithNotes

@Composable
fun FolderAccordionItem(
    folderWithNotes: FolderWithNotes,
    onNoteClick: (Long) -> Unit,
    onLongPress: (Folder) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .background(
                color = Color(0xFFEEEEEE),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { isExpanded = !isExpanded },
                    onLongClick = { onLongPress(folderWithNotes.folder) }
                )
                .padding(16.dp)
        ) {
            Text(
                text = folderWithNotes.folder.name
            )

            Text(
                text = folderWithNotes.notes.size.toString()
            )
        }
        if (isExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp)
            ) {
                folderWithNotes.notes.forEach { note ->
                    NoteCard(
                        note,
                        onClick = { onNoteClick(note.id) }
                    )
                }
            }
        }
    }
}