package com.vn.kaygv.notetaking.ui.components

import com.vn.kaygv.notetaking.ui.editor.markdown.MarkdownTransformation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun BlockTextField(
    blockId: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onEnterPressed: (TextFieldValue) -> Unit,
    onKeyEvent: (KeyEvent, TextFieldValue) -> Boolean,
    focusedBlockId: String?,
    textStyle: TextStyle = LocalTextStyle.current,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDone: (TextFieldValue) -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val uriHandler = LocalUriHandler.current

    // Keyed on blockId so state resets when the block identity changes (e.g. after a merge)
    var internalValue by remember(blockId) { mutableStateOf(value) }

    // Sync external changes back in (selection repositioning, merge results, etc.)
    LaunchedEffect(value) {
        if (internalValue.text != value.text || internalValue.selection != value.selection) {
            internalValue = value
        }
    }

    LaunchedEffect(focusedBlockId) {
        if (focusedBlockId == blockId) {
            focusRequester.requestFocus()
        }
    }

    // Capture text layout so we can map tap positions → character offsets
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicTextField(
        value = internalValue,
        onValueChange = { newValue ->
            if (singleLine) {
                val clean = newValue.copy(text = newValue.text.replace("\n", ""))
                internalValue = clean
                onValueChange(clean)
                return@BasicTextField
            }
            // Detect Enter via newline insertion
            val newlineIndex = newValue.text.indexOfAny(charArrayOf('\n', '\r'))
            if (newlineIndex != -1 && !internalValue.text.contains("\n") && !internalValue.text.contains("\r")) {
                val withoutNewline = newValue.text.replace("\n", "").replace("\r", "")
                val updatedValue = TextFieldValue(withoutNewline, TextRange(newlineIndex))
                internalValue = updatedValue
                onValueChange(updatedValue)
                onEnterPressed(updatedValue)
            } else {
                internalValue = newValue
                onValueChange(newValue)
            }
        },
        textStyle = textStyle.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
        ),
        keyboardActions = KeyboardActions(onDone = { onDone(internalValue) }),
        onTextLayout = { textLayoutResult = it },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                onKeyEvent(event, internalValue)
            }
            // Link-click detector: observe taps before BasicTextField consumes them.
            .pointerInput(blockId) {
                detectTapGestures(
                    onLongPress = { offset ->
                        textLayoutResult?.let { layout ->
                            val charOffset = layout.getOffsetForPosition(offset)
                            val newValue = internalValue.copy(selection = TextRange(charOffset))
                            internalValue = newValue
                            onLongPress()
                            onValueChange(newValue)
                        }
                    },
                    onTap = { offset ->
                        val layout = textLayoutResult ?: return@detectTapGestures
                        val charOffset = layout.getOffsetForPosition(offset)

                        // Check for URL annotation at the tapped character offset.
                        val url = layout.layoutInput.text
                            .getStringAnnotations(
                                tag = "URL",
                                start = charOffset,
                                end = charOffset + 1
                            )
                            .firstOrNull()
                            ?.item

                        if (url != null) {
                            val finalUrl = if (!url.contains("://")) "https://$url" else url
                            try {
                                uriHandler.openUri(finalUrl)
                            } catch (e: Exception) {}
                        } else {
                            // Focus and move cursor to tap position
                            focusRequester.requestFocus()
                            val newValue = internalValue.copy(selection = TextRange(charOffset))
                            internalValue = newValue
                            onValueChange(newValue)
                        }
                    }
                )
            }
    )
}

