package com.vn.kaygv.notetaking.ui.components.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vn.kaygv.notetaking.domain.model.Folder
import com.vn.kaygv.notetaking.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerBottomSheet(
    selectedNoteFolderId: Long?,
    folders: List<Folder>,
    newFolderName: String,
    isCreating: Boolean,
    onStartCreate: () -> Unit,
    onUpdateFolderName: (String) -> Unit,
    onCreate: () -> Unit,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Select Folder",
                style = Typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Folder list
            folders.forEach { folder ->
                Text(
                    text = folder.name,
                    fontWeight = if (folder.id == selectedNoteFolderId) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = folder.id != selectedNoteFolderId
                        ) { onSelect(folder.id) }
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isCreating) {

                TextField(
                    value = newFolderName,
                    onValueChange = onUpdateFolderName,
                    placeholder = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                ) {
                    Text(
                        text = "Cancel",
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                    Text(
                        text = "Create",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onCreate() }
                            .padding(8.dp)
                    )
                }

            } else {

                Text(
                    text = "+ Create new folder",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartCreate() }
                        .padding(12.dp)
                )
            }
        }
    }
}