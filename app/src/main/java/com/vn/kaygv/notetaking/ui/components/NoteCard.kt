package com.vn.kaygv.notetaking.ui.components

import android.graphics.Typeface
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.vn.kaygv.notetaking.domain.model.Note
import androidx.core.graphics.drawable.toDrawable

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val previewText = remember(note.content) {
        note.content.text
            .lineSequence()
            .drop(1)
            .joinToString(" ")
            .trim()
            .take(300)
    }

    val rotation = remember(note.id) {
        ((note.id.hashCode() % 6) - 3).toFloat()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .graphicsLayer {
                rotationZ = rotation
                shadowElevation = 12f
                shape = RoundedCornerShape(4.dp)
                clip = false
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = previewText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AdCard(ad: NativeAd) {

    val rotation = remember(ad.hashCode()) {
        ((ad.hashCode() % 6) - 3).toFloat()
    }

    val backgroundColor = MaterialTheme.colorScheme.surface
    val titleColor = MaterialTheme.colorScheme.onSurface
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.secondary
    val paddingValue = with(LocalDensity.current) {
        16.dp.toPx().toInt()
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .graphicsLayer {
                rotationZ = rotation
                shadowElevation = 12f
                shape = RoundedCornerShape(4.dp)
                clip = false
            },
        factory = { context ->

            val adView = NativeAdView(context)

            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(paddingValue, paddingValue, paddingValue, paddingValue)
                background = backgroundColor.toArgb().toDrawable()
            }

            val sponsoredView = TextView(context).apply {
                text = "Sponsored"
                setTextColor(accentColor.toArgb())
                textSize = 12f
            }

            val headlineView = TextView(context).apply {
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(titleColor.toArgb())
                textSize = 16f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }

            val bodyView = TextView(context).apply {
                setTextColor(bodyColor.toArgb())
                textSize = 14f
                maxLines = 3
                ellipsize = TextUtils.TruncateAt.END
            }

            container.addView(sponsoredView)
            container.addView(headlineView)
            container.addView(bodyView)

            adView.addView(container)

            // ✅ Correct asset binding
            adView.headlineView = headlineView
            adView.bodyView = bodyView

            adView
        },
        update = { adView ->

            val headline = adView.headlineView as TextView
            val body = adView.bodyView as TextView

            headline.text = ad.headline
            body.text = ad.body

            adView.setNativeAd(ad)
        }
    )
}