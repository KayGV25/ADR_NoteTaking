package com.kaygv.notetaking.domain.model

import androidx.compose.ui.text.input.TextFieldValue

data class Note(
    val id: Long = 0,
    val title: String,
    val content: TextFieldValue,
    val folderId: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
