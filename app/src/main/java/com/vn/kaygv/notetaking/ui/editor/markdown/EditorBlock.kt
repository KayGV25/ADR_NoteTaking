package com.vn.kaygv.notetaking.ui.editor.markdown

import androidx.compose.ui.text.input.TextFieldValue
import java.util.UUID

sealed class EditorBlock {
    abstract val id: String

    data class Paragraph(
        override val id: String = UUID.randomUUID().toString(),
        val value: TextFieldValue
    ) : EditorBlock()

    data class Heading(
        override val id: String = UUID.randomUUID().toString(),
        val level: Int = 1,
        val value: TextFieldValue
    ) : EditorBlock()

    data class Checkbox(
        override val id: String = UUID.randomUUID().toString(),
        val checked: Boolean,
        val value: TextFieldValue,
        val indent: Int = 0
    ) : EditorBlock()

    data class Bullet(
        override val id: String = UUID.randomUUID().toString(),
        val indent: Int = 0,
        val value: TextFieldValue
    ) : EditorBlock()

    data class Numbered(
        override val id: String = UUID.randomUUID().toString(),
        val indent: Int = 0,
        val number: Int = 1,
        val value: TextFieldValue
    ) : EditorBlock()

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        val url: String
    ) : EditorBlock()
}

fun parseMarkdownToBlocks(text: String): List<EditorBlock> {
    return text.lines().map { line ->
        when {
            line.startsWith("### ") ->
                EditorBlock.Heading(level = 3, value = TextFieldValue(line.removePrefix("### ")))

            line.startsWith("## ") ->
                EditorBlock.Heading(level = 2, value = TextFieldValue(line.removePrefix("## ")))

            line.startsWith("# ") ->
                EditorBlock.Heading(level = 1, value = TextFieldValue(line.removePrefix("# ")))

            line.matches(Regex("""\s*-\s\[[ x]]\s+.*""")) -> {
                val indent = line.takeWhile { it == ' ' }.length / 2
                val checked = line.contains("- [x]")

                val text = line.replaceFirst(Regex("""\s*-\s\[[ x]]\s+"""), "")

                EditorBlock.Checkbox(
                    checked = checked,
                    indent = indent,
                    value = TextFieldValue(text)
                )
            }

            line.matches(Regex("""\s*-\s+.*""")) -> {
                val indent = line.takeWhile { it == ' ' }.length / 2
                val text = line.replaceFirst(Regex("""\s*-\s+"""), "")

                EditorBlock.Bullet(
                    indent = indent,
                    value = TextFieldValue(text)
                )
            }


            line.matches(Regex("""!\[.*?]\((.*?)\)""")) -> {
                val url = Regex("""\((.*?)\)""")
                    .find(line)?.groupValues?.get(1) ?: ""
                EditorBlock.Image(url = url)
            }

            line.matches(Regex("""\s*\d+\.\s+.*""")) -> {
                val indent = line.takeWhile { it == ' ' }.length / 2
                val number = Regex("""\d+""").find(line)?.value?.toInt() ?: 1
                val text = line.replaceFirst(Regex("""\s*\d+\.\s+"""), "")

                EditorBlock.Numbered(
                    indent = indent,
                    number = number,
                    value = TextFieldValue(text)
                )
            }

            else ->
                EditorBlock.Paragraph(value = TextFieldValue(line))
        }
    }
}

fun blocksToMarkdown(blocks: List<EditorBlock>): String {
    return blocks.joinToString("\n") { block ->
        when (block) {
            is EditorBlock.Paragraph -> block.value.text

            is EditorBlock.Heading ->
                "#".repeat(block.level) + " " + block.value.text

            is EditorBlock.Checkbox ->
                "  ".repeat(block.indent) +
                        if (block.checked)
                            "- [x] ${block.value.text}"
                        else
                            "- [ ] ${block.value.text}"


            is EditorBlock.Bullet ->
                "${"  ".repeat(block.indent)}- ${block.value.text}"

            is EditorBlock.Numbered ->
                "${"  ".repeat(block.indent)}${block.number}. ${block.value.text}"


            is EditorBlock.Image ->
                "![](${block.url})"
        }
    }
}
