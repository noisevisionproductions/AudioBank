package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun FiltersWindow(isExpanded: Boolean, expandedHeight: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(expandedHeight)
            .background(colors.backgroundGrayColor)
            .pointerInput(isExpanded) {
                if (!isExpanded) {
                    detectTapGestures {}
                }
            }
    ) {
        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DropDownMenuWithItems("Instrumenty")
                DropDownMenuWithItems("Rodzaje")
                DropDownMenuWithItems("Ton")
                DropDownMenuWithItems("BPM")
            }
        }
    }
}

@Composable
fun DropDownMenuWithItems(label: String) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(label) }

    Column {
        Button(onClick = { expanded = !expanded }) {
            Text(selectedItem)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 1"
                expanded = false
            }) {
                Text("Opcja 1")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 2"
                expanded = false
            }) {
                Text("Opcja 2")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 3"
                expanded = false
            }) {
                Text("Opcja 3")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 4"
                expanded = false
            }) {
                Text("Opcja 4")
            }
        }
    }
}

@Composable
fun ScrollingText(fileName: String, modifier: Modifier = Modifier) {
    var textWidth by remember { mutableStateOf(0f) }
    var containerWidth by remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(fileName, textWidth, containerWidth) {
        if (textWidth > containerWidth) {
            offsetX.snapTo(containerWidth)
            offsetX.animateTo(
                targetValue = -textWidth,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (textWidth * 25).toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            offsetX.snapTo(0f)
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
            .padding(end = 5.dp, start = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        containerWidth = constraints.maxWidth.toFloat()

        Box(modifier = Modifier.clipToBounds()) {
            val textMeasurer = rememberTextMeasurer()
            val textLayoutResult = remember(fileName) {
                textMeasurer.measure(
                    text = AnnotatedString(fileName),
                    style = TextStyle(fontSize = 16.sp)
                )
            }
            textWidth = textLayoutResult.size.width.toFloat()

            Text(
                text = fileName,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                fontSize = 16.sp,
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.toInt(), 0) }
                    .width(with(LocalDensity.current) { textWidth.toDp() })
            )
        }
    }
}

@Composable
expect fun PropertiesMenu(
    fileUrl: String,
    fileName: String,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit,
    alignRight: Boolean
)