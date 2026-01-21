package com.drew654.mocklocations.presentation.map_screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun Crosshairs(
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = modifier
            .size(64.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.width / 3

        drawCircle(
            color = color,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        drawLine(
            color = color,
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = strokeWidth
        )

        drawLine(
            color = color,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = strokeWidth
        )
    }
}
