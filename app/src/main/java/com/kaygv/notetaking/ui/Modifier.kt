package com.kaygv.notetaking.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

fun Modifier.fadingEdge(edge: FadeEdge, length: Float = 100f) =
    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {

            drawContent()

            val brush = when (edge) {

                FadeEdge.TOP -> Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = length
                )

                FadeEdge.BOTTOM -> Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - length,
                    endY = size.height
                )

                FadeEdge.LEFT -> Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = 0f,
                    endX = length
                )

                FadeEdge.RIGHT -> Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = size.width - length,
                    endX = size.width
                )

                FadeEdge.VERTICAL -> Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black,
                        Color.Black,
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height
                )

                FadeEdge.HORIZONTAL -> Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black,
                        Color.Black,
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = size.width
                )
            }

            drawRect(brush = brush, blendMode = BlendMode.DstIn)
        }


enum class FadeEdge { TOP, BOTTOM, LEFT, RIGHT, VERTICAL, HORIZONTAL }