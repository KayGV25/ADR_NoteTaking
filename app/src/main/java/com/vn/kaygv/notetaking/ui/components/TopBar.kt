package com.vn.kaygv.notetaking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vn.kaygv.notetaking.ui.theme.Typography

@Composable
fun TopBar(
    title: String,
    leadingIcon: @Composable () -> Unit = { },
    trailingIcon: @Composable () -> Unit = { }
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 32.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = Typography.titleLarge
            )
        }
        trailingIcon()
    }
}