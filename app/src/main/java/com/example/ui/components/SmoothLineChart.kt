package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb

@Composable
fun SmoothLineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.White,
    gridColor: Color = Color.White.copy(alpha = 0.12f),
    drawDot: Boolean = true,
    dotColor: Color = Color(0xFFEA3B35), // Vibrant Red
    drawTooltip: Boolean = true
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOfOrNull { it.second } ?: 1.0
    val minVal = data.minOfOrNull { it.second } ?: 0.0
    val safeMax = if (maxVal == minVal) maxVal + 1.0 else maxVal
    val range = safeMax - minVal

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val width = size.width
        val height = size.height

        // Horizontal grid lines
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = height * 0.15f + (height * 0.7f) * (i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Compute point coordinates
        val points = data.mapIndexed { index, pair ->
            val x = if (data.size > 1) {
                width * 0.05f + (width * 0.9f) * (index.toFloat() / (data.size - 1))
            } else {
                width / 2f
            }
            val normValue = (pair.second - minVal) / range
            val y = height * 0.85f - (height * 0.65f) * normValue.toFloat()
            Offset(x, y)
        }

        if (points.isNotEmpty()) {
            val path = Path()
            path.moveTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val conX1 = p0.x + (p1.x - p0.x) / 2f
                val conY1 = p0.y
                val conX2 = p0.x + (p1.x - p0.x) / 2f
                val conY2 = p1.y
                path.cubicTo(conX1, conY1, conX2, conY2, p1.x, p1.y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw selection dot and tooltip on one of the points
            if (drawDot) {
                // Focus on the second to last index, or last index
                val targetIndex = (points.size - 2).coerceAtLeast(0)
                if (targetIndex < points.size) {
                    val dotPoint = points[targetIndex]
                    val amount = data[targetIndex].second

                    // Pulse outer ring
                    drawCircle(
                        color = dotColor.copy(alpha = 0.25f),
                        radius = 8.dp.toPx(),
                        center = dotPoint
                    )
                    // Inner solid dot
                    drawCircle(
                        color = dotColor,
                        radius = 4.dp.toPx(),
                        center = dotPoint
                    )

                    // Red tooltip tag
                    if (drawTooltip) {
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = dotColor.toArgb()
                                style = Paint.Style.FILL
                                isAntiAlias = true
                            }
                            val textPaint = Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 9.dp.toPx()
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                isAntiAlias = true
                                textAlign = Paint.Align.CENTER
                            }

                            val tooltipText = "৳${String.format(java.util.Locale.US, "%.0f", amount)}"
                            val rectWidth = textPaint.measureText(tooltipText) + 12.dp.toPx()
                            val rectHeight = 16.dp.toPx()

                            val rx = dotPoint.x
                            val ry = dotPoint.y - 20.dp.toPx()

                            // Tooltip box
                            canvas.nativeCanvas.drawRoundRect(
                                rx - rectWidth / 2f,
                                ry - rectHeight / 2f,
                                rx + rectWidth / 2f,
                                ry + rectHeight / 2f,
                                4.dp.toPx(),
                                4.dp.toPx(),
                                paint
                            )

                            // Tooltip text
                            canvas.nativeCanvas.drawText(
                                tooltipText,
                                rx,
                                ry + rectHeight / 4f,
                                textPaint
                            )
                        }
                    }
                }
            }
        }
    }
}
