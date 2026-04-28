package com.vn.kaygv.notetaking.ui.editor.markdown

import androidx.compose.ui.graphics.Color
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

        val o2t = mutableListOf<Int>() // original index → transformed index
        val t2o = mutableListOf<Int>() // transformed index → original index

        var tIndex = 0

        fun skipOriginal(upToExclusive: Int) {
            while (o2t.size < upToExclusive) {
                o2t.add(tIndex)
            }
        }

        fun appendMapped(char: Char, originalIndex: Int) {
            skipOriginal(originalIndex)
            o2t.add(tIndex)
            t2o.add(originalIndex)
            output.append(char)
            tIndex++
        }

        val lines = raw.split("\n")
        var oIndex = 0

        fun parseInline(line: String, lineOBase: Int) {
            var i = 0
            while (i < line.length) {
                val remaining = line.substring(i)

                // Try to find a style marker
                val styleMatch = when {
                    remaining.startsWith("***") -> Triple("***", 3, SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                    remaining.startsWith("**") -> Triple("**", 2, SpanStyle(fontWeight = FontWeight.Bold))
                    remaining.startsWith("__") -> Triple("__", 2, SpanStyle(textDecoration = TextDecoration.Underline))
                    remaining.startsWith("*") -> Triple("*", 1, SpanStyle(fontStyle = FontStyle.Italic))
                    remaining.startsWith("~~") -> Triple("~~", 2, SpanStyle(textDecoration = TextDecoration.LineThrough))
                    else -> null
                }

                if (styleMatch != null) {
                    val (marker, mLen, style) = styleMatch
                    val end = line.indexOf(marker, i + mLen)
                    if (end != -1) {
                        // Found a pair!
                        // 1. Hide the opening marker
                        skipOriginal(lineOBase + i)
                        repeat(mLen) { o2t.add(tIndex) }

                        // 2. Push style and parse inner content recursively
                        output.pushStyle(style)
                        parseInline(line.substring(i + mLen, end), lineOBase + i + mLen)
                        output.pop()

                        // 3. Hide the closing marker
                        skipOriginal(lineOBase + end)
                        repeat(mLen) { o2t.add(tIndex) }

                        i = end + mLen
                        continue
                    }
                }

                // Handle links
                if (remaining.startsWith("[")) {
                    val endText = line.indexOf("]", i + 1)
                    val startUrl = if (endText != -1 && endText + 1 < line.length && line[endText + 1] == '(') endText + 1 else -1
                    val endUrl = if (startUrl != -1) line.indexOf(")", startUrl + 1) else -1

                    if (endText != -1 && startUrl != -1 && endUrl != -1) {
                        val textPart = line.substring(i + 1, endText)
                        val urlPart = line.substring(startUrl + 1, endUrl)

                        // Hide '['
                        skipOriginal(lineOBase + i)
                        o2t.add(tIndex)

                        output.pushStringAnnotation(tag = "URL", annotation = urlPart)
                        output.pushStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline))

                        // Recursively parse text inside the link
                        parseInline(textPart, lineOBase + i + 1)

                        output.pop()
                        output.pop()

                        // Hide '](url)'
                        skipOriginal(lineOBase + endText)
                        repeat(endUrl - endText + 1) { o2t.add(tIndex) }

                        i = endUrl + 1
                        continue
                    }
                }

                // Default: append character
                appendMapped(line[i], lineOBase + i)
                i++
            }
        }

        lines.forEachIndexed { lineIndex, line ->
            var i = 0
            var hasBlockStyle = false

            when {
                line.startsWith("### ") -> {
                    i = 4; hasBlockStyle = true
                    output.pushStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                }
                line.startsWith("## ") -> {
                    i = 3; hasBlockStyle = true
                    output.pushStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                }
                line.startsWith("# ") -> {
                    i = 2; hasBlockStyle = true
                    output.pushStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
                }
                line.startsWith("- [ ] ") -> {
                    output.pushStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                    "☐ ".forEachIndexed { idx, c -> appendMapped(c, oIndex + idx) }
                    output.pop()
                    i = 6
                }
                line.startsWith("- [x] ") -> {
                    output.pushStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                    "☑ ".forEachIndexed { idx, c -> appendMapped(c, oIndex + idx) }
                    output.pop()
                    i = 6
                }
                line.startsWith("- ") -> {
                    "• ".forEachIndexed { idx, c -> appendMapped(c, oIndex + idx) }
                    i = 2
                }
            }

            parseInline(line.substring(i), oIndex + i)

            if (hasBlockStyle) output.pop()

            if (lineIndex != lines.lastIndex) {
                output.append("\n")
                skipOriginal(oIndex + line.length)
                o2t.add(tIndex)
                t2o.add(oIndex + line.length)
                tIndex++
            }

            oIndex += line.length + 1
        }

        // Sentinel: ensure o2t covers raw.length and t2o has a final entry
        skipOriginal(raw.length)
        o2t.add(tIndex)
        t2o.add(raw.length)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                o2t.getOrElse(offset) { o2t.lastOrNull() ?: 0 }.coerceIn(0, tIndex)

            override fun transformedToOriginal(offset: Int): Int =
                t2o.getOrElse(offset) { t2o.lastOrNull() ?: raw.length }.coerceIn(0, raw.length)
        }

        return TransformedText(output.toAnnotatedString(), offsetMapping)
    }
}
