package com.vn.kaygv.notetaking.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.vn.kaygv.notetaking.ui.folder.FolderWithNotes

@Composable
fun FolderItem(
    folderWithNotes: FolderWithNotes,
    isOpen: Boolean,
    onClick: (FolderWithNotes) -> Unit,
    onLongPress: (FolderWithNotes) -> Unit,
    modifier: Modifier = Modifier
) {
    val noteCount = folderWithNotes.notes.size
    val animatedFrontHeight by animateFloatAsState(
        targetValue = if (isOpen) {
            0.5f
        } else {
            when (noteCount) {
                0 -> 0.85f
                1 -> 0.8f
                2 -> 0.75f
                else -> 0.7f
            }
        },
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "folder_open_anim"
    )
    val previewCount = minOf(3, noteCount)
    val spread by animateFloatAsState(if (isOpen) 1f else 0f)

    Box(
        modifier = modifier
            .aspectRatio(1.3f)
            .combinedClickable(
                onClick = {
                    onClick(folderWithNotes)
                },
                onLongClick = { onLongPress(folderWithNotes) }
            )
            .padding(24.dp)
    ) {

        // ===== BACK =====
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(6.dp, FolderShape(cornerRadius = 16.dp))
                .clip(FolderShape())
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF5C4F4A), Color(0xFF2B2623))
                    )
                )
        )

        repeat(previewCount) { index ->

            val rotation = when (index) {
                0 -> -8f
                1 -> 0f
                else -> 8f
            }

            val offsetY = (index * 4 - 8).dp

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .fillMaxHeight(0.5f)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        rotationZ = rotation + spread
                        translationY = offsetY.toPx() - spread
                        translationX = when (index) {
                            0 -> -6.dp.toPx() - 0.5f * spread
                            1 -> 0f
                            else -> 6.dp.toPx() + 0.5f * spread
                        }
                        scaleX = 1f + 0.1f * spread
                        scaleY = 1f + 0.1f * spread
                    }
                    .shadow(4.dp, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .border(0.5.dp, Color.LightGray, RoundedCornerShape(4.dp))

            )
        }

        // ===== FRONT =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(animatedFrontHeight)
                .align(Alignment.BottomCenter)
                .shadow(
                    if (isOpen) 16.dp else 8.dp,
                    RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF5C4F4A), Color(0xFF2B2623))
                    )
                )
        )

        // ===== FOLDER NAME =====
        Text(
            text = folderWithNotes.folder.name,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            color = Color.White
        )
    }
}

class FolderShape(
    private val flapStartFraction: Float = 0.5f,
    private val flapWidthFraction: Float = 0.18f,
    private val flapHeightFraction: Float = 0.07f,
    private val cornerRadius: Dp = 8.dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {

        val flapStartX = size.width * flapStartFraction
        val flapW = size.width * flapWidthFraction

        val flapEndX = (flapStartX + flapW).coerceAtMost(size.width)
        val actualFlapW = flapEndX - flapStartX

        val flapH = size.height * flapHeightFraction
        val r = with(density) { cornerRadius.toPx() }
            .coerceAtMost(size.minDimension / 2)

        val path = Path().apply {

            moveTo(0f, flapH + r)

            lineTo(0f, flapH + r)

            quadraticTo(
                0f, flapH,
                r, flapH
            )

            lineTo(flapStartX, flapH)

            // ===== FLAP =====
            cubicTo(
                flapStartX + actualFlapW * 0.25f, flapH * 1.2f,
                flapStartX + actualFlapW * 0.75f, flapH * 0.15f,
                flapStartX + actualFlapW, 0f
            )

            // ===== TOP EDGE → rounded top-right =====
            lineTo(size.width - r, 0f)
            quadraticTo(
                size.width, 0f,
                size.width, r
            )

            // ===== RIGHT SIDE =====
            lineTo(size.width, size.height - r)

            // ===== BOTTOM RIGHT CORNER =====
            quadraticTo(
                size.width, size.height,
                size.width - r, size.height
            )

            // ===== BOTTOM EDGE =====
            lineTo(r, size.height)

            // ===== BOTTOM LEFT CORNER =====
            quadraticTo(
                0f, size.height,
                0f, size.height - r
            )

            // ===== LEFT SIDE BACK TO START =====
            lineTo(0f, flapH)

            close()
        }

        return Outline.Generic(path)
    }
}
