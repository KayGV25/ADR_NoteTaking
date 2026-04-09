package com.kaygv.notetaking.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.kaygv.notetaking.R

@Composable
fun EditorToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onCheckbox: () -> Unit,
    onBullet: () -> Unit,
    onNumbered: () -> Unit,
    onInsertImage: () -> Unit,
    onInsertLink: () -> Unit,
    onIndent: () -> Unit,
    onOutdent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ToolbarButton(
                text = "Bold",
                icon = ImageVector.vectorResource(R.drawable.format_bold_24px)
            ) { onBold() }

            ToolbarButton(
                text = "Italic",
                icon = ImageVector.vectorResource(R.drawable.format_italic_24px)
            ) { onItalic() }

            ToolbarButton(
                text = "Underline",
                icon = ImageVector.vectorResource(R.drawable.format_underlined_24px)
            ) { onUnderline() }

            ToolbarButton(
                text = "Indent",
                icon = ImageVector.vectorResource(R.drawable.format_indent_increase_24px)
            ) { onIndent() }

            ToolbarButton(
                text = "Outdent",
                icon = ImageVector.vectorResource(R.drawable.format_indent_decrease_24px)
            ) { onOutdent() }

            ToolbarButton(
                text = "Check Box",
                icon = ImageVector.vectorResource(R.drawable.check_box_24px)
            ) { onCheckbox() }

            ToolbarButton(
                text = "Bullet List",
                icon = ImageVector.vectorResource(R.drawable.format_list_bulleted_24px)
            ) { onBullet() }

            ToolbarButton(
                text = "Numbered List",
                icon = ImageVector.vectorResource(R.drawable.format_list_numbered_24px)
            ) { onNumbered() }

            ToolbarButton(
                text = "Image",
                icon = ImageVector.vectorResource(R.drawable.image_24px)
            ) { onInsertImage() }

            ToolbarButton(
                text = "Link",
                icon = ImageVector.vectorResource(R.drawable.link_24px)
            ) { onInsertLink() }
        }
    }
}

@Composable
fun ToolbarButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Icon(
        icon,
        contentDescription = text,
        modifier = Modifier
            .size(36.dp)
            .padding(8.dp)
            .clickable { onClick() },
    )
}
