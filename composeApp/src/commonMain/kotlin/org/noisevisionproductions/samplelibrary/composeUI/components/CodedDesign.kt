package org.noisevisionproductions.samplelibrary.composeUI.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.sp
import org.noisevisionproductions.samplelibrary.composeUI.screens.FragmentsTabs
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
                color = colors.primaryBackgroundColor.copy(alpha = 0.4f),
                radius = size.minDimension / 3,
                center = Offset(
                    x = size.width * 0.25f,
                    y = size.height * 0.4f
                )
            )
            // Rysowanie drugiego kółka
            drawCircle(
                color = colors.primaryBackgroundColor.copy(alpha = 0.4f),
                radius = size.minDimension / 3,
                center = Offset(
                    x = size.width * 0.6f,
                    y = size.height * 0.2f
                )
            )
        }
    }
}

fun Modifier.topShadow() = this
    .graphicsLayer(clip = false)
    .drawWithContent {
        drawContent()
        val shadowHeight = 8.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.4f)
                ),
                startY = -shadowHeight,
                endY = 0f
            ),
            topLeft = Offset(0f, -shadowHeight),
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
