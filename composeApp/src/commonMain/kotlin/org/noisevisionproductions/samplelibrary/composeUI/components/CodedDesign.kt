package org.noisevisionproductions.samplelibrary.composeUI.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors


@Composable
fun BackgroundWithCircles(
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Canvas(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-130).dp)
                .align(Alignment.TopStart)
        ) {
            // Rysowanie pierwszego kółka
            drawCircle(
                color = colors.primaryBackgroundColor.copy(alpha = 0.4f), // Kolor kółka z przezroczystością
                radius = size.minDimension / 3, // Rozmiar kółka
                center = Offset(
                    x = size.width * 0.25f,
                    y = size.height * 0.4f
                ) // Pozycja kółka
            )
            // Rysowanie drugiego kółka
            drawCircle(
                color = colors.primaryBackgroundColor.copy(alpha = 0.4f), // Kolor kółka z przezroczystością
                radius = size.minDimension / 3, // Rozmiar drugiego kółka
                center = Offset(
                    x = size.width * 0.6f,
                    y = size.height * 0.2f
                ) // Pozycja drugiego kółka
            )
        }
    }
}

@Composable
fun BackgroundWithCirclesWithAvatar(
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    // Use the modifier passed in and apply a background color
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Create a Canvas that fills the available space
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Define circle radius as a fraction of canvas height
            val circleRadius = canvasHeight * 0.7f  // Circles will extend beyond the canvas

            // Draw the first circle in the top-left corner
            drawCircle(
                color = Color(0x70B6E7E7),
                radius = circleRadius,
                center = Offset(
                    x = -circleRadius * 0.0f,
                    y = -circleRadius * -0.2f
                )
            )

            // Draw the second circle overlapping the first one
            drawCircle(
                color = Color(0x70B6E7E7),
                radius = circleRadius,
                center = Offset(
                    x = canvasWidth * 0.25f,
                    y = -circleRadius * 0.2f
                )
            )
        }
    }
}

fun Modifier.bottomShadow() = this
    .graphicsLayer(clip = false) // Allow drawing outside the bounds
    .drawWithContent {
        drawContent() // Draw the Row's content first
        // Now draw the shadow
        val shadowHeight = 12.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                startY = size.height,
                endY = size.height + shadowHeight
            ),
            topLeft = Offset(0f, size.height),
            size = Size(size.width, shadowHeight)
        )
    }

@Composable
fun DefaultAvatar(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Default Avatar",
        modifier = modifier.size(70.dp),
        tint = colors.backgroundGrayColor,
    )
}
