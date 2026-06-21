package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val maxVal = data.maxOfOrNull { it.second } ?: 1.0
    val safeMax = if (maxVal == 0.0) 1.0 else maxVal
    val colors = listOf(
        Color(0xFFEA3B35), // Accent Vibrant Red
        Color(0xFF020203), // Near Black
        Color(0xFF767677), // Muted Gray
        Color(0xFF94A3B8), // Slate Gray
        Color(0xFFE5E5E2)  // Soft Light Gray
    )

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val width = size.width
        val height = size.height
        
        val barWidth = width / (data.size * 2)
        val spacing = barWidth
        
        var currentX = spacing / 2
        
        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value / safeMax * height).toFloat()
            val safeBarHeight = if (barHeight.isNaN() || barHeight < 0f) 0f else barHeight
            val color = colors[index % colors.size]
            
            // Draw background track (using light grey card surface color)
            drawRoundRect(
                color = Color(0xFFE5E5E2),
                topLeft = Offset(currentX, height - (height * 0.8f).toFloat()),
                size = Size(barWidth, (height * 0.8f).toFloat()),
                cornerRadius = CornerRadius(12f, 12f)
            )
            
            // Draw value bar (from bottom)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.copy(alpha = 0.85f))
                ),
                topLeft = Offset(currentX, height - safeBarHeight),
                size = Size(barWidth, safeBarHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )
            currentX += barWidth + spacing
        }
    }
}
