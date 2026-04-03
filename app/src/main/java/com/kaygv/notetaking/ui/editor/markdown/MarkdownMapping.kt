package com.kaygv.notetaking.ui.editor.markdown

data class MarkdownMapping(
    val originalToTransformed: (Int) -> Int,
    val transformedToOriginal: (Int) -> Int
)

fun headerMapping(prefixLength: Int): MarkdownMapping {
    return MarkdownMapping(
        originalToTransformed = { offset ->
            (offset - prefixLength).coerceAtLeast(0)
        },
        transformedToOriginal = { offset ->
            offset + prefixLength
        }
    )
}

