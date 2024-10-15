package org.noisevisionproductions.samplelibrary.composeUI.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors


@Composable
fun BackgroundWithCircles() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundDarkGrayColor)
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
fun DefaultAvatar() {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Default Avatar",
        modifier = Modifier.size(70.dp),
        tint = colors.backgroundGrayColor
    )
}
