package com.vn.kaygv.notetaking.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun NotePreview(notePreviewConfig: NotePreviewConfig) {
    Dialog(onDismissRequest = { notePreviewConfig.onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Note Preview Window
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        notePreviewConfig.onPreviewClick()
                    }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = notePreviewConfig.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Floating Action Menu
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    for (button in notePreviewConfig.buttons) {
                        TextButton(
                            onClick = {
                                button.onClick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(button.icon, contentDescription = button.text)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(button.text, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    TextButton(
                        onClick = {
                            notePreviewConfig.onDelete()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Note", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

class NotePreviewConfig(
    val content: AnnotatedString,
    val onDismiss: () -> Unit,
    val onPreviewClick: () -> Unit,
    val buttons: List<NotePreviewButtonConfig> = emptyList(),
    val onDelete: () -> Unit
)

class NotePreviewButtonConfig(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)