package com.kaygv.notetaking.ui.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.kaygv.notetaking.domain.model.Note
import com.kaygv.notetaking.ui.components.NoteCard
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteAction
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteDialog
import com.kaygv.notetaking.ui.dialog.noteDialog.NoteDialogHost
import com.kaygv.notetaking.ui.folder.FolderWithNotes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBottomSheet(
    folder: FolderWithNotes,
    onNoteClick: (Long) -> Unit,
    onNoteLongPress: (Note) -> Unit,
    onDismiss: () -> Unit,

    dialog: NoteDialog,
    onDialogDismiss: () -> Unit,
    onDialogAction: (NoteAction) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            DrawerHeader()

            Text(
                text = folder.folder.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(folder.notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onLongClick = { onNoteLongPress(note) }
                    )
                }
            }
        }
    }

    NoteDialogHost(
        dialog = dialog,
        onDismiss = onDialogDismiss,
        onAction = onDialogAction
    )
}

@Composable
fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(72.dp)
                .height(28.dp)
                .shadow(6.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(36.dp)
                .height(4.dp)
                .offset(y = (-8).dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        )
    }
}
