package com.vn.kaygv.notetaking.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun BlockTextField(
    blockId: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onEnterPressed: () -> Unit,
    onKeyEvent: (KeyEvent, TextFieldValue) -> Boolean,
    focusedBlockId: String?,
    textStyle: TextStyle = LocalTextStyle.current,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onDone: () -> Unit = {}
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
            if (newValue.text.contains("\n") && !internalValue.text.contains("\n")) {
                val withoutNewline = newValue.copy(text = newValue.text.replace("\n", ""))
                internalValue = withoutNewline
                onValueChange(withoutNewline)
                onEnterPressed()
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
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        onTextLayout = { textLayoutResult = it },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                onKeyEvent(event, internalValue)
            }
            // Link-click detector: observe taps before BasicTextField consumes them.
            .pointerInput(blockId) {
                awaitEachGesture {
                    // Use Initial pass to see the event before BasicTextField consumes it.
                    val down =
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var up: PointerInputChange? = null

                    // Wait for the Up event on the Initial pass
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null) break
                        if (change.changedToUp()) {
                            up = change
                            break
                        }
                        // If the finger moves significantly, consider it a scroll/drag rather than a tap
                        if ((change.position - down.position).getDistance() > 24f) break
                    }

                    if (up != null) {
                        val layout = textLayoutResult ?: return@awaitEachGesture
                        val charOffset = layout.getOffsetForPosition(up.position)

                        // Check for URL annotation at the tapped character offset.
                        // end = charOffset + 1 ensures the range is not empty so overlaps are detected.
                        val url = layout.layoutInput.text
                            .getStringAnnotations(
                                tag = "URL",
                                start = charOffset,
                                end = charOffset + 1
                            )
                            .firstOrNull()
                            ?.item

                        if (url != null) {
                            up.consume() // Consume the event so BasicTextField doesn't move the cursor
                            val finalUrl = if (!url.contains("://")) "https://$url" else url
                            try {
                                uriHandler.openUri(finalUrl)
                            } catch (e: Exception) {

                            }
                        }
                    }
                }
            }
    )
}

