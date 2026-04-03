package com.kaygv.notetaking.ui.editor.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

class MarkdownTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {

        val raw = text.text
        val output = AnnotatedString.Builder()

        val o2t = mutableListOf<Int>() // original -> transformed
        val t2o = mutableListOf<Int>() // transformed -> original

        var tIndex = 0

        fun map(originalIndex: Int) {
            // Ensure o2t is filled up to this original index
            while (o2t.size <= originalIndex) {
                o2t.add(tIndex)
            }

            // Map transformed → original (1:1 with appended chars)
            t2o.add(originalIndex)

            // Move transformed cursor forward
            tIndex++
        }


        fun appendMapped(char: Char, originalIndex: Int) {
            output.append(char)
            map(originalIndex)
        }

        val lines = raw.split("\n")
        var oIndex = 0

        lines.forEachIndexed { lineIndex, line ->

            var i = 0
            var hasBlockStyle = false

            // ------------------------
            // BLOCK PARSING
            // ------------------------

            when {
                line.startsWith("### ") -> {
                    i = 4
                    output.pushStyle(
                        SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    )
                    hasBlockStyle = true
                }

                line.startsWith("## ") -> {
                    i = 3
                    output.pushStyle(
                        SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    )
                    hasBlockStyle = true
                }

                line.startsWith("# ") -> {
                    i = 2
                    output.pushStyle(
                        SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )
                    hasBlockStyle = true
                }

                line.startsWith("- [ ] ") -> {
                    val visible = "☐ "
                    visible.forEachIndexed { idx, c ->
                        appendMapped(c, oIndex + idx)
                    }
                    i = 6
                }

                line.startsWith("- [x] ") -> {
                    val visible = "☑ "
                    visible.forEachIndexed { idx, c ->
                        appendMapped(c, oIndex + idx)
                    }
                    i = 6
                }

                line.startsWith("- ") -> {
                    val visible = "• "
                    visible.forEachIndexed { idx, c ->
                        appendMapped(c, oIndex + idx)
                    }
                    i = 2
                }
            }

            // ------------------------
            // INLINE PARSING
            // ------------------------

            while (i < line.length) {

                when {

                    // **bold**
                    line.startsWith("**", i) -> {
                        val end = line.indexOf("**", i + 2)
                        if (end != -1) {

                            val content = line.substring(i + 2, end)

                            output.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))

                            content.forEachIndexed { idx, c ->
                                appendMapped(c, oIndex + i + 2 + idx)
                            }

                            output.pop()
                            i = end + 2
                            continue
                        }
                    }

                    // *italic*
                    line.startsWith("*", i) -> {
                        val end = line.indexOf("*", i + 1)
                        if (end != -1) {

                            val content = line.substring(i + 1, end)

                            output.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))

                            content.forEachIndexed { idx, c ->
                                appendMapped(c, oIndex + i + 1 + idx)
                            }

                            output.pop()
                            i = end + 1
                            continue
                        }
                    }

                    // ~~strike~~
                    line.startsWith("~~", i) -> {
                        val end = line.indexOf("~~", i + 2)
                        if (end != -1) {

                            val content = line.substring(i + 2, end)

                            output.pushStyle(
                                SpanStyle(textDecoration = TextDecoration.LineThrough)
                            )

                            content.forEachIndexed { idx, c ->
                                appendMapped(c, oIndex + i + 2 + idx)
                            }

                            output.pop()
                            i = end + 2
                            continue
                        }
                    }

                    else -> {
                        appendMapped(line[i], oIndex + i)
                        i++
                    }
                }
            }

            if (hasBlockStyle) {
                output.pop()
            }

            if (lineIndex != lines.lastIndex) {
                output.append("\n")
                map(oIndex + line.length)
            }

            oIndex += line.length + 1
        }

        while (o2t.size <= raw.length) {
            o2t.add(tIndex)
        }
        t2o.add(raw.length)

        val offsetMapping = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                return o2t.getOrElse(offset) { o2t.lastOrNull() ?: 0 }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return t2o.getOrElse(offset) { t2o.lastOrNull() ?: raw.length }
            }
        }

        return TransformedText(output.toAnnotatedString(), offsetMapping)
    }
}