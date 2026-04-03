package com.kaygv.notetaking.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaygv.notetaking.domain.model.Folder
import com.kaygv.notetaking.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerBottomSheet(
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

            // No folder option
//            Text(
//                text = "No Folder",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { onSelect(null) }
//                    .padding(12.dp)
//            )

            // Folder list
            folders.forEach { folder ->
                Text(
                    text = folder.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(folder.id) }
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 Create Folder Section
            if (isCreating) {

                TextField(
                    value = newFolderName,
                    onValueChange = onUpdateFolderName,
                    placeholder = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Text(
                        text = "Cancel",
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Create",
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